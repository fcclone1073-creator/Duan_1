const express = require('express');
const mongoose = require('mongoose');
const Order = require('../models/Order');

const router = express.Router();

const buildResponse = (success, message, data = null) => ({
  success,
  message,
  data
});

const calculateTotalAmount = (items = []) =>
  items.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0);

const populateOrder = (query) =>
  query
    .populate('user', 'name email')
    .populate('items.product', 'name price image');

// Danh sách đơn hàng
router.get('/', async (req, res) => {
  try {
    const orders = await populateOrder(
      Order.find().sort({ createdAt: -1 })
    );
    res.json(buildResponse(true, 'Danh sách đơn hàng', orders));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Đơn hàng theo người dùng
router.get('/user/:userId/list', async (req, res) => {
  try {
    const { userId } = req.params;
    if (!mongoose.Types.ObjectId.isValid(userId)) {
      return res.status(400).json(buildResponse(false, 'User ID không hợp lệ'));
    }

    const page = Math.max(parseInt(req.query.page || '1', 10), 1);
    const limit = Math.max(parseInt(req.query.limit || '10', 10), 1);
    const status = (req.query.status || '').trim();

    const filter = { user: userId };
    if (status) filter.status = status;

    const total = await Order.countDocuments(filter);
    const orders = await populateOrder(
      Order.find(filter)
        .sort({ createdAt: -1 })
        .skip((page - 1) * limit)
        .limit(limit)
    );

    res.json(
      buildResponse(true, 'Danh sách đơn hàng của người dùng', {
        items: orders,
        total,
        page,
        limit,
        pages: Math.ceil(total / limit),
        filter: { status: status || undefined }
      })
    );
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Chi tiết đơn hàng
router.get('/detail/:id', async (req, res) => {
  try {
    const { id } = req.params;
    if (!mongoose.Types.ObjectId.isValid(id)) {
      return res.status(400).json(buildResponse(false, 'Order ID không hợp lệ'));
    }
    const order = await populateOrder(Order.findById(id));
    if (!order) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy đơn hàng'));
    }
    res.json(buildResponse(true, 'Chi tiết đơn hàng', order));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Top sản phẩm bán chạy
router.get('/top-products', async (req, res) => {
  try {
    const limit = Math.max(parseInt(req.query.limit || '5', 10), 1);
    const status = (req.query.status || 'delivered').trim();
    const start = req.query.start ? new Date(req.query.start) : null;
    const end = req.query.end ? new Date(req.query.end) : null;

    const match = {};
    if (status) match.status = status;
    if (start || end) match.createdAt = {};
    if (start) match.createdAt.$gte = start;
    if (end) match.createdAt.$lte = end;

    const results = await Order.aggregate([
      { $match: match },
      { $unwind: '$items' },
      {
        $group: {
          _id: '$items.product',
          sold: { $sum: { $ifNull: ['$items.quantity', 0] } },
          revenue: { $sum: { $multiply: [{ $ifNull: ['$items.quantity', 0] }, { $ifNull: ['$items.price', 0] }] } },
          orderIds: { $addToSet: '$_id' }
        }
      },
      { $project: { sold: 1, revenue: 1, orderCount: { $size: '$orderIds' } } },
      { $sort: { sold: -1, revenue: -1 } },
      { $limit: limit },
      {
        $lookup: {
          from: 'products',
          localField: '_id',
          foreignField: '_id',
          as: 'product'
        }
      },
      { $unwind: '$product' },
      {
        $project: {
          product: { _id: '$product._id', name: '$product.name', image: '$product.image', price: '$product.price', stock: '$product.stock' },
          sold: 1,
          revenue: 1,
          orderCount: 1
        }
      }
    ]);

    res.json(
      buildResponse(true, 'Top sản phẩm bán chạy', {
        items: results,
        limit,
        filter: { status: status || undefined, start: start || undefined, end: end || undefined }
      })
    );
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Top khách hàng
router.get('/top-customers', async (req, res) => {
  try {
    const limit = Math.max(parseInt(req.query.limit || '5', 10), 1);
    const status = (req.query.status || 'delivered').trim();
    const start = req.query.start ? new Date(req.query.start) : null;
    const end = req.query.end ? new Date(req.query.end) : null;

    const match = {};
    if (status) match.status = status;
    if (start || end) match.createdAt = {};
    if (start) match.createdAt.$gte = start;
    if (end) match.createdAt.$lte = end;

    const results = await Order.aggregate([
      { $match: match },
      {
        $group: {
          _id: '$user',
          orderCount: { $sum: 1 },
          totalSpend: { $sum: { $ifNull: ['$totalAmount', 0] } },
          lastOrder: { $max: '$createdAt' }
        }
      },
      { $sort: { totalSpend: -1, orderCount: -1, lastOrder: -1 } },
      { $limit: limit },
      {
        $lookup: {
          from: 'users',
          localField: '_id',
          foreignField: '_id',
          as: 'user'
        }
      },
      { $unwind: '$user' },
      {
        $project: {
          user: { _id: '$user._id', name: '$user.name', email: '$user.email' },
          orderCount: 1,
          totalSpend: 1,
          lastOrder: 1
        }
      }
    ]);

    res.json(
      buildResponse(true, 'Top khách hàng', {
        items: results,
        limit,
        filter: { status: status || undefined, start: start || undefined, end: end || undefined }
      })
    );
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Tạo đơn hàng
router.post('/', async (req, res) => {
  try {
    const { user, items, shippingAddress, status } = req.body;

    if (!user || !items || items.length === 0 || !shippingAddress) {
      return res.status(400).json(buildResponse(false, 'Thiếu thông tin đơn hàng'));
    }

    const order = await Order.create({
      user,
      items,
      shippingAddress,
      status: status || 'pending',
      totalAmount: calculateTotalAmount(items)
    });

    const populatedOrder = await populateOrder(Order.findById(order._id));
    res.status(201).json(buildResponse(true, 'Tạo đơn hàng thành công', populatedOrder));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// Cập nhật đơn hàng
router.put('/:id', async (req, res) => {
  try {
    const updatePayload = { ...req.body, updatedAt: new Date() };
    if (req.body.items) {
      updatePayload.totalAmount = calculateTotalAmount(req.body.items);
    }

    const order = await Order.findByIdAndUpdate(
      req.params.id,
      updatePayload,
      { new: true, runValidators: true }
    );

    if (!order) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy đơn hàng'));
    }

    const populatedOrder = await populateOrder(Order.findById(order._id));
    res.json(buildResponse(true, 'Cập nhật đơn hàng thành công', populatedOrder));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// Xóa đơn hàng
router.delete('/:id', async (req, res) => {
  try {
    const order = await Order.findByIdAndDelete(req.params.id);
    if (!order) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy đơn hàng'));
    }
    res.json(buildResponse(true, 'Xóa đơn hàng thành công', order));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

module.exports = router;
