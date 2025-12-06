const express = require('express');
const Notification = require('../models/Notification');

const router = express.Router();

const buildResponse = (success, message, data = null) => ({
  success,
  message,
  data
});

// Gửi thông báo hệ thống (Admin)
router.post('/', async (req, res) => {
  try {
    const { title, message, type, targetUser, createdBy } = req.body;

    if (!title || !message || !createdBy) {
      return res.status(400).json(buildResponse(false, 'Thiếu thông tin thông báo'));
    }

    const notification = await Notification.create({
      title,
      message,
      type: type || 'system',
      targetUser: targetUser || null, // null = gửi cho tất cả
      createdBy
    });

    res.status(201).json(buildResponse(true, 'Gửi thông báo thành công', notification));
  } catch (error) {
    res.status(400).json(buildResponse(false, error.message));
  }
});

// Danh sách thông báo của user
router.get('/user/:userId', async (req, res) => {
  try {
    const { unreadOnly } = req.query;
    const query = {
      $or: [
        { targetUser: req.params.userId },
        { targetUser: null } // Thông báo hệ thống cho tất cả
      ]
    };

    if (unreadOnly === 'true') {
      query.isRead = false;
    }

    const notifications = await Notification.find(query)
      .populate('createdBy', 'name email')
      .sort({ createdAt: -1 });

    res.json(buildResponse(true, 'Danh sách thông báo', notifications));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Đánh dấu đã đọc
router.put('/:id/read', async (req, res) => {
  try {
    const notification = await Notification.findByIdAndUpdate(
      req.params.id,
      { isRead: true, readAt: new Date() },
      { new: true }
    );

    if (!notification) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy thông báo'));
    }

    res.json(buildResponse(true, 'Đã đánh dấu đã đọc', notification));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Đánh dấu tất cả đã đọc
router.put('/user/:userId/read-all', async (req, res) => {
  try {
    const result = await Notification.updateMany(
      {
        $or: [
          { targetUser: req.params.userId },
          { targetUser: null }
        ],
        isRead: false
      },
      { isRead: true, readAt: new Date() }
    );

    res.json(buildResponse(true, `Đã đánh dấu ${result.modifiedCount} thông báo đã đọc`));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Xóa thông báo
router.delete('/:id', async (req, res) => {
  try {
    const notification = await Notification.findByIdAndDelete(req.params.id);
    if (!notification) {
      return res.status(404).json(buildResponse(false, 'Không tìm thấy thông báo'));
    }
    res.json(buildResponse(true, 'Đã xóa thông báo', notification));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

// Đếm thông báo chưa đọc
router.get('/user/:userId/unread-count', async (req, res) => {
  try {
    const count = await Notification.countDocuments({
      $or: [
        { targetUser: req.params.userId },
        { targetUser: null }
      ],
      isRead: false
    });

    res.json(buildResponse(true, 'Số thông báo chưa đọc', { count }));
  } catch (error) {
    res.status(500).json(buildResponse(false, error.message));
  }
});

module.exports = router;

