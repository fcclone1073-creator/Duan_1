const express = require('express');
const Product = require('../models/Product');

const router = express.Router();

const buildResponse = (success, message, data = null) => ({
  success,
  message,
  data
});

// Danh sách sản phẩm với tìm kiếm và lọc
router.get('/', async (req, res) => {
  try {
    const { 
      search,           // Tìm kiếm theo tên
      category,         // Lọc theo danh mục
      minPrice,         // Giá tối thiểu
      maxPrice,         // Giá tối đa
      inStock,          // Còn hàng (true/false)
      sortBy,           // Sắp xếp: price_asc, price_desc, name_asc, name_desc, newest
      page = 1,         // Trang hiện tại
      limit = 20        // Số lượng mỗi trang
    } = req.query;

    // Xây dựng query
    const query = {};

    // Tìm kiếm theo tên
    if (search) {
      query.name = { $regex: search, $options: 'i' };
    }

    // Lọc theo danh mục
    if (category) {
      query.category = category;
    }

    // Lọc theo giá
    if (minPrice || maxPrice) {
      query.price = {};
      if (minPrice) query.price.$gte = Number(minPrice);
      if (maxPrice) query.price.$lte = Number(maxPrice);
    }

    // Lọc theo tồn kho
    if (inStock === 'true') {
      query.stock = { $gt: 0 };
    } else if (inStock === 'false') {
      query.stock = { $lte: 0 };
    }

    // Xây dựng sort
    let sort = { createdAt: -1 }; // Mặc định sắp xếp mới nhất
    if (sortBy) {
      switch (sortBy) {
        case 'price_asc':
          sort = { price: 1 };
          break;
        case 'price_desc':
          sort = { price: -1 };
          break;
        case 'name_asc':
          sort = { name: 1 };
          break;
        case 'name_desc':
          sort = { name: -1 };
          break;
        case 'newest':
          sort = { createdAt: -1 };
          break;
        case 'oldest':
          sort = { createdAt: 1 };
          break;
      }
    }

    // Phân trang
    const skip = (Number(page) - 1) * Number(limit);
    const total = await Product.countDocuments(query);
    const products = await Product.find(query)
      .populate('category', 'name')
      .sort(sort)
      .skip(skip)
      .limit(Number(limit));

    res.json(buildResponse(true, 'Danh sách sản phẩm', {
      products,
      pagination: {
        currentPage: Number(page),
        totalPages: Math.ceil(total / Number(limit)),
        totalItems: total,
        itemsPerPage: Number(limit)
      },
      filters: {
        search: search || null,
        category: category || null,
        minPrice: minPrice || null,
        maxPrice: maxPrice || null,
        inStock: inStock || null,
        sortBy: sortBy || 'newest'
      }
    }));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Chi tiết sản phẩm
router.get('/:id', async (req, res) => {
  try {
    const product = await Product.findById(req.params.id).populate('category', 'name');
    if (!product) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm'));
    }
    res.json(buildResponse(true, 'Chi tiết sản phẩm', product));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Thêm sản phẩm
router.post('/', async (req, res) => {
  try {
    const product = await Product.create(req.body);
    res.status(201).json(buildResponse(true, 'Thêm sản phẩm thành công', product));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// Cập nhật sản phẩm
router.put('/:id', async (req, res) => {
  try {
    const product = await Product.findByIdAndUpdate(
      req.params.id,
      { ...req.body, updatedAt: new Date() },
      { new: true, runValidators: true }
    );

    if (!product) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm'));
    }

    res.json(buildResponse(true, 'Cập nhật sản phẩm thành công', product));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// Xóa sản phẩm
router.delete('/:id', async (req, res) => {
  try {
    const product = await Product.findByIdAndDelete(req.params.id);
    if (!product) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm'));
    }
    res.json(buildResponse(true, 'Xóa sản phẩm thành công', product));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

module.exports = router;
