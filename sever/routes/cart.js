const express = require('express');
const mongoose = require('mongoose');
const GioHang = require('../models/GioHang');
const GioHangChiTiet = require('../models/GioHangChiTiet');
const Cart = require('../models/Cart');
const Product = require('../models/Product');

const router = express.Router();

const buildResponse = (success, message, data = null) => ({ success, message, data });

const computeTotal = async (gioHangId) => {
  const items = await GioHangChiTiet.find({ idGioHang: gioHangId });
  return items.reduce((sum, it) => sum + (it.donGia || 0) * (it.soLuong || 0), 0);
};

const getCartDetail = async (userId) => {
  let cart = await GioHang.findOne({ idKH: userId });
  if (!cart) {
    cart = await GioHang.create({ idKH: userId, tongGia: 0 });
  }
  const items = await GioHangChiTiet.find({ idGioHang: cart._id })
    .populate('idCTSP', 'name price image');
  const formattedItems = items.map((it) => ({
    id: it._id,
    product: it.idCTSP,
    soLuong: it.soLuong,
    donGia: it.donGia,
    thanhTien: (it.donGia || 0) * (it.soLuong || 0),
    ngayTao: it.ngayTao,
    ngayCapNhat: it.ngayCapNhat
  }));
  const tongGia = formattedItems.reduce((sum, it) => sum + it.thanhTien, 0);
  if (tongGia !== cart.tongGia) {
    cart.tongGia = tongGia;
    await cart.save();
  }
  return {
    id: cart._id,
    idKH: cart.idKH,
    tongGia: cart.tongGia,
    ngayTao: cart.ngayTao,
    ngayCapNhat: cart.ngayCapNhat,
    items: formattedItems
  };
};

router.get('/', async (req, res) => {
  try {
    const userId = (req.query.userId || '').trim();
    if (!mongoose.Types.ObjectId.isValid(userId)) {
      return res.status(400).json(buildResponse(false, 'User ID không hợp lệ'));
    }
    const data = await getCartDetail(userId);
    res.json(buildResponse(true, 'Giỏ hàng hiện tại', data));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

router.delete('/', async (req, res) => {
  try {
    const userId = (req.query.userId || '').trim();
    if (!mongoose.Types.ObjectId.isValid(userId)) {
      return res.status(400).json(buildResponse(false, 'User ID không hợp lệ'));
    }
    const cart = await GioHang.findOne({ idKH: userId });
    if (!cart) {
      return res.json(buildResponse(true, 'Đã xóa giỏ hàng', null));
    }
    await GioHangChiTiet.deleteMany({ idGioHang: cart._id });
    await GioHang.findByIdAndDelete(cart._id);
    res.json(buildResponse(true, 'Đã xóa giỏ hàng', null));
// =======
// const buildResponse = (success, message, data = null) => ({
//   success,
//   message,
//   data
// });

// // Lấy giỏ hàng của user
// router.get('/user/:userId', async (req, res) => {
//   try {
//     let cart = await Cart.findOne({ user: req.params.userId })
//       .populate('items.product', 'name price image stock');

//     if (!cart) {
//       // Tạo giỏ hàng mới nếu chưa có
//       cart = await Cart.create({ user: req.params.userId, items: [] });
//       cart = await Cart.findById(cart._id)
//         .populate('items.product', 'name price image stock');
//     }

//     // Tính tổng tiền
//     let totalAmount = 0;
//     cart.items.forEach(item => {
//       if (item.product) {
//         totalAmount += item.product.price * item.quantity;
//       }
//     });

//     const cartData = cart.toObject();
//     res.json(buildResponse(true, 'Giỏ hàng', {
//       ...cartData,
//       totalAmount
//     }));
// >>>>>>> 3f8aaddc73892d73dcd74905b2ff593a3a621411
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// <<<<<<< HEAD
router.post('/items', async (req, res) => {
  try {
    const { userId, productId, soLuong } = req.body;
    if (!mongoose.Types.ObjectId.isValid(userId) || !mongoose.Types.ObjectId.isValid(productId)) {
      return res.status(400).json(buildResponse(false, 'ID không hợp lệ'));
    }
    const qty = Math.max(parseInt(soLuong || '1', 10), 1);
    let cart = await GioHang.findOne({ idKH: userId });
    if (!cart) cart = await GioHang.create({ idKH: userId, tongGia: 0 });
// =======
// // Thêm sản phẩm vào giỏ hàng
// router.post('/user/:userId/item', async (req, res) => {
//   try {
//     const { productId, quantity = 1 } = req.body;

//     if (!productId) {
//       return res.status(400).json(buildResponse(false, 'Thiếu thông tin sản phẩm'));
//     }

//     // Kiểm tra sản phẩm tồn tại
// >>>>>>> 3f8aaddc73892d73dcd74905b2ff593a3a621411
    const product = await Product.findById(productId);
    if (!product) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm'));
    }
// <<<<<<< HEAD
    const existing = await GioHangChiTiet.findOne({ idGioHang: cart._id, idCTSP: productId });
    if (existing) {
      existing.soLuong += qty;
      await existing.save();
    } else {
      await GioHangChiTiet.create({ idGioHang: cart._id, idCTSP: productId, soLuong: qty, donGia: product.price });
    }
    cart.tongGia = await computeTotal(cart._id);
    await cart.save();
    const data = await getCartDetail(userId);
    res.status(201).json(buildResponse(true, 'Đã thêm vào giỏ hàng', data));
// =======

//     // Kiểm tra tồn kho
//     if (product.stock < quantity) {
//       return res.status(400).json(buildResponse(false, 'Số lượng sản phẩm không đủ'));
//     }

//     let cart = await Cart.findOne({ user: req.params.userId });

//     if (!cart) {
//       cart = await Cart.create({ user: req.params.userId, items: [] });
//     }

//     // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
//     const existingItemIndex = cart.items.findIndex(
//       item => item.product && item.product.toString() === productId
//     );

//     if (existingItemIndex !== -1) {
//       // Cập nhật số lượng
//       cart.items[existingItemIndex].quantity += quantity;
//       if (product.stock < cart.items[existingItemIndex].quantity) {
//         return res.status(400).json(buildResponse(false, 'Số lượng sản phẩm không đủ'));
//       }
//     } else {
//       // Thêm mới
//       cart.items.push({ product: productId, quantity });
//     }

//     cart.updatedAt = new Date();
//     await cart.save();

//     const populatedCart = await Cart.findById(cart._id)
//       .populate('items.product', 'name price image stock');

//     res.json(buildResponse(true, 'Đã thêm vào giỏ hàng', populatedCart));
// >>>>>>> 3f8aaddc73892d73dcd74905b2ff593a3a621411
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// <<<<<<< HEAD
router.put('/items/:productId', async (req, res) => {
  try {
    const { productId } = req.params;
    const { userId, soLuong } = req.body;
    if (!mongoose.Types.ObjectId.isValid(userId) || !mongoose.Types.ObjectId.isValid(productId)) {
      return res.status(400).json(buildResponse(false, 'ID không hợp lệ'));
    }
    const qty = Math.max(parseInt(soLuong || '1', 10), 0);
    const cart = await GioHang.findOne({ idKH: userId });
    if (!cart) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy giỏ hàng'));
    }
    const item = await GioHangChiTiet.findOne({ idGioHang: cart._id, idCTSP: productId });
    if (!item) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy item trong giỏ'));
    }
    if (qty === 0) {
      await GioHangChiTiet.deleteOne({ _id: item._id });
    } else {
      item.soLuong = qty;
      await item.save();
    }
    cart.tongGia = await computeTotal(cart._id);
    await cart.save();
    const data = await getCartDetail(userId);
    res.json(buildResponse(true, 'Đã cập nhật item', data));
// =======
// // Cập nhật số lượng sản phẩm trong giỏ hàng
// router.put('/user/:userId/item/:productId', async (req, res) => {
//   try {
//     const { quantity } = req.body;

//     if (!quantity || quantity < 1) {
//       return res.status(400).json(buildResponse(false, 'Số lượng không hợp lệ'));
//     }

//     const cart = await Cart.findOne({ user: req.params.userId });
//     if (!cart) {
//       return res.status(404).json(buildResponse(false, 'Không tìm thấy giỏ hàng'));
//     }

//     const itemIndex = cart.items.findIndex(
//       item => item.product && item.product.toString() === req.params.productId
//     );

//     if (itemIndex === -1) {
//       return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm trong giỏ hàng'));
//     }

//     // Kiểm tra tồn kho
//     const product = await Product.findById(req.params.productId);
//     if (!product) {
//       return res.status(404).json(buildResponse(false, 'Không tìm thấy sản phẩm'));
//     }
//     if (product.stock < quantity) {
//       return res.status(400).json(buildResponse(false, 'Số lượng sản phẩm không đủ'));
//     }

//     cart.items[itemIndex].quantity = quantity;
//     cart.updatedAt = new Date();
//     await cart.save();

//     const populatedCart = await Cart.findById(cart._id)
//       .populate('items.product', 'name price image stock');

//     res.json(buildResponse(true, 'Đã cập nhật giỏ hàng', populatedCart));
// >>>>>>> 3f8aaddc73892d73dcd74905b2ff593a3a621411
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// <<<<<<< HEAD
router.delete('/items/:productId', async (req, res) => {
  try {
    const { productId } = req.params;
    const userId = (req.query.userId || '').trim();
    if (!mongoose.Types.ObjectId.isValid(userId) || !mongoose.Types.ObjectId.isValid(productId)) {
      return res.status(400).json(buildResponse(false, 'ID không hợp lệ'));
    }
    const cart = await GioHang.findOne({ idKH: userId });
    if (!cart) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy giỏ hàng'));
    }
    await GioHangChiTiet.deleteOne({ idGioHang: cart._id, idCTSP: productId });
    cart.tongGia = await computeTotal(cart._id);
    await cart.save();
    const data = await getCartDetail(userId);
    res.json(buildResponse(true, 'Đã xóa item', data));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
// =======
// // Xóa sản phẩm khỏi giỏ hàng
// router.delete('/user/:userId/item/:productId', async (req, res) => {
//   try {
//     const cart = await Cart.findOne({ user: req.params.userId });
//     if (!cart) {
//       return res.status(404).json(buildResponse(false, 'Không tìm thấy giỏ hàng'));
//     }

//     cart.items = cart.items.filter(
//       item => !item.product || item.product.toString() !== req.params.productId
//     );
//     cart.updatedAt = new Date();
//     await cart.save();

//     const populatedCart = await Cart.findById(cart._id)
//       .populate('items.product', 'name price image stock');

//     res.json(buildResponse(true, 'Đã xóa khỏi giỏ hàng', populatedCart));
//   } catch (error) {
//     res.status(500).json(buildResponse(false, error.message));
//   }
// });

// // Xóa toàn bộ giỏ hàng
// router.delete('/user/:userId', async (req, res) => {
//   try {
//     const cart = await Cart.findOneAndDelete({ user: req.params.userId });
//     if (!cart) {
//       return res.status(404).json(buildResponse(false, 'Không tìm thấy giỏ hàng'));
//     }
//     res.json(buildResponse(true, 'Đã xóa giỏ hàng', cart));
//   } catch (error) {
//     res.status(500).json(buildResponse(false, error.message));
// >>>>>>> 3f8aaddc73892d73dcd74905b2ff593a3a621411
  }
});

module.exports = router;

