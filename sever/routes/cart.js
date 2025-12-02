const express = require('express');
const Cart = require('../models/Cart');
const Product = require('../models/Product');

const router = express.Router();

const buildResponse = (success, message, data = null) => ({
  success,
  message,
  data
});

// Lấy giỏ hàng của user
router.get('/user/:userId', async (req, res) => {
  try {
    let cart = await Cart.findOne({ user: req.params.userId })
      .populate('items.product', 'name price image stock');

    if (!cart) {
      // Tạo giỏ hàng mới nếu chưa có
      cart = await Cart.create({ user: req.params.userId, items: [] });
      cart = await Cart.findById(cart._id)
        .populate('items.product', 'name price image stock');
    }

    // Tính tổng tiền
    let totalAmount = 0;
    cart.items.forEach(item => {
      if (item.product) {
        totalAmount += item.product.price * item.quantity;
      }
    });

    const cartData = cart.toObject();
    res.json(buildResponse(true, 'Giỏ hàng', {
      ...cartData,
      totalAmount
    }));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Thêm sản phẩm vào giỏ hàng
router.post('/user/:userId/item', async (req, res) => {
  try {
    const { productId, quantity = 1 } = req.body;

    if (!productId) {
      return res.status(400).json(buildResponse(false, 'Thiếu thông tin sản phẩm'));
    }

    // Kiểm tra sản phẩm tồn tại
    const product = await Product.findById(productId);
    if (!product) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm'));
    }

    // Kiểm tra tồn kho
    if (product.stock < quantity) {
      return res.status(400).json(buildResponse(false, 'Số lượng sản phẩm không đủ'));
    }

    let cart = await Cart.findOne({ user: req.params.userId });

    if (!cart) {
      cart = await Cart.create({ user: req.params.userId, items: [] });
    }

    // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
    const existingItemIndex = cart.items.findIndex(
      item => item.product && item.product.toString() === productId
    );

    if (existingItemIndex !== -1) {
      // Cập nhật số lượng
      cart.items[existingItemIndex].quantity += quantity;
      if (product.stock < cart.items[existingItemIndex].quantity) {
        return res.status(400).json(buildResponse(false, 'Số lượng sản phẩm không đủ'));
      }
    } else {
      // Thêm mới
      cart.items.push({ product: productId, quantity });
    }

    cart.updatedAt = new Date();
    await cart.save();

    const populatedCart = await Cart.findById(cart._id)
      .populate('items.product', 'name price image stock');

    res.json(buildResponse(true, 'Đã thêm vào giỏ hàng', populatedCart));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// Cập nhật số lượng sản phẩm trong giỏ hàng
router.put('/user/:userId/item/:productId', async (req, res) => {
  try {
    const { quantity } = req.body;

    if (!quantity || quantity < 1) {
      return res.status(400).json(buildResponse(false, 'Số lượng không hợp lệ'));
    }

    const cart = await Cart.findOne({ user: req.params.userId });
    if (!cart) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy giỏ hàng'));
    }

    const itemIndex = cart.items.findIndex(
      item => item.product && item.product.toString() === req.params.productId
    );

    if (itemIndex === -1) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm trong giỏ hàng'));
    }

    // Kiểm tra tồn kho
    const product = await Product.findById(req.params.productId);
    if (!product) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm'));
    }
    if (product.stock < quantity) {
      return res.status(400).json(buildResponse(false, 'Số lượng sản phẩm không đủ'));
    }

    cart.items[itemIndex].quantity = quantity;
    cart.updatedAt = new Date();
    await cart.save();

    const populatedCart = await Cart.findById(cart._id)
      .populate('items.product', 'name price image stock');

    res.json(buildResponse(true, 'Đã cập nhật giỏ hàng', populatedCart));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// Xóa sản phẩm khỏi giỏ hàng
router.delete('/user/:userId/item/:productId', async (req, res) => {
  try {
    const cart = await Cart.findOne({ user: req.params.userId });
    if (!cart) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy giỏ hàng'));
    }

    cart.items = cart.items.filter(
      item => !item.product || item.product.toString() !== req.params.productId
    );
    cart.updatedAt = new Date();
    await cart.save();

    const populatedCart = await Cart.findById(cart._id)
      .populate('items.product', 'name price image stock');

    res.json(buildResponse(true, 'Đã xóa khỏi giỏ hàng', populatedCart));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Xóa toàn bộ giỏ hàng
router.delete('/user/:userId', async (req, res) => {
  try {
    const cart = await Cart.findOneAndDelete({ user: req.params.userId });
    if (!cart) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy giỏ hàng'));
    }
    res.json(buildResponse(true, 'Đã xóa giỏ hàng', cart));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

module.exports = router;

