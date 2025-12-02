const express = require('express');
const Order = require('../models/Order');
const Product = require('../models/Product');
const User = require('../models/User');
const Review = require('../models/Review');
const Category = require('../models/Category');

const router = express.Router();

const buildResponse = (success, message, data = null) => ({
  success,
  message,
  data
});

// Thống kê tổng quan
router.get('/overview', async (req, res) => {
  try {
    const [
      totalUsers,
      totalProducts,
      totalOrders,
      totalRevenue,
      totalCategories,
      totalReviews
    ] = await Promise.all([
      User.countDocuments(),
      Product.countDocuments(),
      Order.countDocuments(),
      Order.aggregate([
        { $match: { status: { $ne: 'cancelled' } } },
        { $group: { _id: null, total: { $sum: '$totalAmount' } } }
      ]),
      Category.countDocuments(),
      Review.countDocuments()
    ]);

    const revenue = totalRevenue[0]?.total || 0;

    res.json(buildResponse(true, 'Thống kê tổng quan', {
      totalUsers,
      totalProducts,
      totalOrders,
      totalRevenue: revenue,
      totalCategories,
      totalReviews
    }));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Thống kê doanh thu theo thời gian
router.get('/revenue', async (req, res) => {
  try {
    const { startDate, endDate, groupBy = 'day' } = req.query;

    const matchStage = { status: { $ne: 'cancelled' } };
    if (startDate || endDate) {
      matchStage.createdAt = {};
      if (startDate) matchStage.createdAt.$gte = new Date(startDate);
      if (endDate) matchStage.createdAt.$lte = new Date(endDate);
    }

    let groupFormat = '%Y-%m-%d';
    if (groupBy === 'month') groupFormat = '%Y-%m';
    if (groupBy === 'year') groupFormat = '%Y';

    const revenue = await Order.aggregate([
      { $match: matchStage },
      {
        $group: {
          _id: { $dateToString: { format: groupFormat, date: '$createdAt' } },
          totalRevenue: { $sum: '$totalAmount' },
          orderCount: { $sum: 1 }
        }
      },
      { $sort: { _id: 1 } }
    ]);

    res.json(buildResponse(true, 'Thống kê doanh thu', revenue));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Xếp hạng sản phẩm bán chạy
router.get('/products/top-selling', async (req, res) => {
  try {
    const { limit = 10 } = req.query;

    const topProducts = await Order.aggregate([
      { $unwind: '$items' },
      { $match: { status: { $ne: 'cancelled' } } },
      {
        $group: {
          _id: '$items.product',
          totalSold: { $sum: '$items.quantity' },
          totalRevenue: { $sum: { $multiply: ['$items.price', '$items.quantity'] } }
        }
      },
      { $sort: { totalSold: -1 } },
      { $limit: Number(limit) },
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
          productId: '$_id',
          productName: '$product.name',
          productImage: '$product.image',
          productPrice: '$product.price',
          totalSold: 1,
          totalRevenue: 1
        }
      }
    ]);

    res.json(buildResponse(true, 'Top sản phẩm bán chạy', topProducts));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Xếp hạng sản phẩm theo đánh giá
router.get('/products/top-rated', async (req, res) => {
  try {
    const { limit = 10 } = req.query;

    const topRated = await Review.aggregate([
      {
        $group: {
          _id: '$product',
          averageRating: { $avg: '$rating' },
          reviewCount: { $sum: 1 }
        }
      },
      { $sort: { averageRating: -1, reviewCount: -1 } },
      { $limit: Number(limit) },
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
          productId: '$_id',
          productName: '$product.name',
          productImage: '$product.image',
          productPrice: '$product.price',
          averageRating: { $round: ['$averageRating', 2] },
          reviewCount: 1
        }
      }
    ]);

    res.json(buildResponse(true, 'Top sản phẩm đánh giá cao', topRated));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Thống kê đơn hàng theo trạng thái
router.get('/orders/status', async (req, res) => {
  try {
    const orderStatus = await Order.aggregate([
      {
        $group: {
          _id: '$status',
          count: { $sum: 1 },
          totalAmount: { $sum: '$totalAmount' }
        }
      },
      { $sort: { count: -1 } }
    ]);

    res.json(buildResponse(true, 'Thống kê đơn hàng theo trạng thái', orderStatus));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Thống kê theo danh mục
router.get('/categories', async (req, res) => {
  try {
    const categoryStats = await Product.aggregate([
      {
        $group: {
          _id: '$category',
          productCount: { $sum: 1 },
          totalStock: { $sum: '$stock' },
          averagePrice: { $avg: '$price' }
        }
      },
      {
        $lookup: {
          from: 'categories',
          localField: '_id',
          foreignField: '_id',
          as: 'category'
        }
      },
      { $unwind: '$category' },
      {
        $project: {
          categoryId: '$_id',
          categoryName: '$category.name',
          productCount: 1,
          totalStock: 1,
          averagePrice: { $round: ['$averagePrice', 2] }
        }
      },
      { $sort: { productCount: -1 } }
    ]);

    res.json(buildResponse(true, 'Thống kê theo danh mục', categoryStats));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

module.exports = router;

