const express = require('express');
const crypto = require('crypto');
const mongoose = require('mongoose');
const User = require('../models/User');

const router = express.Router();

const buildResponse = (success, message, data = null) => ({
  success,
  message,
  data
});

const sanitizeUser = (user) => ({
  id: user._id,
  name: user.name,
  email: user.email,
  role: user.role,
  createdAt: user.createdAt,
  updatedAt: user.updatedAt
});

const hashPassword = (password) => {
  const salt = crypto.randomBytes(16).toString('hex');
  const hash = crypto.pbkdf2Sync(password, salt, 1000, 64, 'sha512').toString('hex');
  return `${salt}:${hash}`;
};

const verifyPassword = (password, storedHash) => {
  const [salt, originalHash] = storedHash.split(':');
  const hash = crypto.pbkdf2Sync(password, salt, 1000, 64, 'sha512').toString('hex');
  return hash === originalHash;
};

const generateToken = () => crypto.randomBytes(32).toString('hex');

// Đăng ký
router.post('/register', async (req, res) => {
  try {
    const { name, email, password } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json(buildResponse(false, 'Vui lòng cung cấp đầy đủ thông tin'));
    }

    if (password.length < 6) {
      return res.status(400).json(buildResponse(false, 'Mật khẩu tối thiểu 6 ký tự'));
    }

    const existingUser = await User.findOne({ email: email.toLowerCase() });
    if (existingUser) {
      return res.status(409).json(buildResponse(false, 'Email đã được sử dụng'));
    }

    const user = await User.create({
      name,
      email: email.toLowerCase(),
      password: hashPassword(password)
    });

    console.log('✅ Đã tạo user mới:', {
      id: user._id,
      name: user.name,
      email: user.email,
      createdAt: user.createdAt
    });

    res.status(201).json(
      buildResponse(true, 'Đăng ký thành công', {
        token: generateToken(),
        user: sanitizeUser(user)
      })
    );
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Đăng nhập
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json(buildResponse(false, 'Email và mật khẩu là bắt buộc'));
    }

    const user = await User.findOne({ email: email.toLowerCase() });
    if (!user) {
      return res.status(401).json(buildResponse(false, 'Email hoặc mật khẩu không đúng'));
    }

    const isValidPassword = verifyPassword(password, user.password);
    if (!isValidPassword) {
      return res.status(401).json(buildResponse(false, 'Email hoặc mật khẩu không đúng'));
    }

    res.json(
      buildResponse(true, 'Đăng nhập thành công', {
        token: generateToken(),
        user: sanitizeUser(user)
      })
    );
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Thêm người dùng (Admin)
router.post('/create', async (req, res) => {
  try {
    const { name, email, password, role } = req.body;
    if (!name || !email || !password) {
      return res.status(400).json(buildResponse(false, 'Vui lòng cung cấp đầy đủ thông tin'));
    }
    if (password.length < 6) {
      return res.status(400).json(buildResponse(false, 'Mật khẩu tối thiểu 6 ký tự'));
    }
    const normalizedEmail = String(email).toLowerCase();
    const existing = await User.findOne({ email: normalizedEmail });
    if (existing) {
      return res.status(400).json(buildResponse(false, 'Email đã được sử dụng'));
    }
    const allowedRoles = ['user', 'admin'];
    const finalRole = allowedRoles.includes(role) ? role : 'user';
    const user = await User.create({
      name,
      email: normalizedEmail,
      password: hashPassword(password),
      role: finalRole
    });
    res.status(200).json(buildResponse(true, 'Đã thêm người dùng', sanitizeUser(user)));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// Danh sách người dùng
router.get('/list', async (req, res) => {
  try {
    const page = Math.max(parseInt(req.query.page || '1', 10), 1);
    const limit = Math.max(parseInt(req.query.limit || '10', 10), 1);
    const search = (req.query.search || '').trim();
    const sortBy = (req.query.sortBy || 'createdAt');
    const order = (req.query.order || 'desc').toLowerCase() === 'asc' ? 1 : -1;

    const filter = search
      ? {
          $or: [
            { name: { $regex: search, $options: 'i' } },
            { email: { $regex: search, $options: 'i' } }
          ]
        }
      : {};

    const total = await User.countDocuments(filter);
    const users = await User.find(filter)
      .sort({ [sortBy]: order })
      .skip((page - 1) * limit)
      .limit(limit);

    res.json(
      buildResponse(true, 'Danh sách người dùng', {
        items: users.map(sanitizeUser),
        total,
        page,
        limit,
        pages: Math.ceil(total / limit),
        sort: { by: sortBy, order: order === 1 ? 'asc' : 'desc' },
        search
      })
    );
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Chi tiết người dùng
router.get('/detail/:id', async (req, res) => {
  try {
    const { id } = req.params;
    if (!mongoose.Types.ObjectId.isValid(id)) {
      return res.status(400).json(buildResponse(false, 'ID không hợp lệ'));
    }
    const user = await User.findById(id);
    if (!user) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy người dùng'));
    }
    res.json(buildResponse(true, 'Chi tiết người dùng', sanitizeUser(user)));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Cập nhật người dùng
router.put('/:id', async (req, res) => {
  try {
    const { name, email, role, password } = req.body;
    const user = await User.findById(req.params.id);

    if (!user) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy người dùng'));
    }

    if (name) user.name = name;
    if (email) user.email = email.toLowerCase();
    if (role) user.role = role;
    if (password) user.password = hashPassword(password);
    user.updatedAt = new Date();

    await user.save();

    res.json(buildResponse(true, 'Cập nhật người dùng thành công', sanitizeUser(user)));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Xóa người dùng
router.delete('/:id', async (req, res) => {
  try {
    const user = await User.findByIdAndDelete(req.params.id);
    if (!user) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy người dùng'));
    }
    res.json(buildResponse(true, 'Đã xóa người dùng', sanitizeUser(user)));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

module.exports = router;
