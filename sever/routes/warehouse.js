const express = require('express');
const Product = require('../models/Product');

const router = express.Router();

const buildResponse = (success, message, data = null) => ({
  success,
  message,
  data
});

// Xem tổng quan kho
router.get('/overview', async (req, res) => {
  try {
    const stats = await Product.aggregate([
      {
        $group: {
          _id: null,
          totalProducts: { $sum: 1 },
          totalStock: { $sum: '$stock' },
          totalValue: { $sum: { $multiply: ['$stock', '$price'] } },
          lowStockCount: {
            $sum: {
              $cond: [{ $lte: ['$stock', 10] }, 1, 0]
            }
          },
          outOfStockCount: {
            $sum: {
              $cond: [{ $eq: ['$stock', 0] }, 1, 0]
            }
          }
        }
      }
    ]);

    res.json(buildResponse(true, 'Tổng quan kho', stats[0] || {
      totalProducts: 0,
      totalStock: 0,
      totalValue: 0,
      lowStockCount: 0,
      outOfStockCount: 0
    }));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Danh sách sản phẩm trong kho
router.get('/products', async (req, res) => {
  try {
    const { 
      lowStock,      // Sản phẩm sắp hết (stock <= 10)
      outOfStock,    // Hết hàng (stock = 0)
      category,      // Lọc theo danh mục
      sortBy,        // Sắp xếp: stock_asc, stock_desc, name_asc
      page = 1,
      limit = 20
    } = req.query;

    const query = {};

    if (lowStock === 'true') {
      query.stock = { $lte: 10, $gt: 0 };
    } else if (outOfStock === 'true') {
      query.stock = { $eq: 0 };
    }

    if (category) {
      query.category = category;
    }

    let sort = { name: 1 };
    if (sortBy) {
      switch (sortBy) {
        case 'stock_asc':
          sort = { stock: 1 };
          break;
        case 'stock_desc':
          sort = { stock: -1 };
          break;
        case 'name_asc':
          sort = { name: 1 };
          break;
      }
    }

    const skip = (Number(page) - 1) * Number(limit);
    const total = await Product.countDocuments(query);
    const products = await Product.find(query)
      .populate('category', 'name')
      .sort(sort)
      .skip(skip)
      .limit(Number(limit));

    res.json(buildResponse(true, 'Danh sách sản phẩm trong kho', {
      products,
      pagination: {
        currentPage: Number(page),
        totalPages: Math.ceil(total / Number(limit)),
        totalItems: total,
        itemsPerPage: Number(limit)
      }
    }));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Cập nhật tồn kho
router.put('/products/:id/stock', async (req, res) => {
  try {
    const { stock, action, quantity } = req.body; // action: 'set', 'add', 'subtract'

    const product = await Product.findById(req.params.id);
    if (!product) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm'));
    }

    if (action === 'add') {
      product.stock += (quantity || 0);
    } else if (action === 'subtract') {
      product.stock = Math.max(0, product.stock - (quantity || 0));
    } else {
      // set
      product.stock = stock || 0;
    }

    product.updatedAt = new Date();
    await product.save();

    res.json(buildResponse(true, 'Đã cập nhật tồn kho', product));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// Sản phẩm sắp hết hàng
router.get('/products/low-stock', async (req, res) => {
  try {
    const { threshold = 10 } = req.query;

    const products = await Product.find({
      stock: { $lte: Number(threshold), $gt: 0 }
    })
      .populate('category', 'name')
      .sort({ stock: 1 });

    res.json(buildResponse(true, 'Sản phẩm sắp hết hàng', products));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Sản phẩm hết hàng
router.get('/products/out-of-stock', async (req, res) => {
  try {
    const products = await Product.find({ stock: 0 })
      .populate('category', 'name')
      .sort({ name: 1 });

    res.json(buildResponse(true, 'Sản phẩm hết hàng', products));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

module.exports = router;

