require('dotenv').config();
const express = require('express');
const connectDB = require('./config/database');

const app = express();

// Kết nối MongoDB trước khi khởi động server
const startServer = async () => {
  try {
    await connectDB();
    
    // Middleware
    app.use(express.json());
    app.use(express.urlencoded({ extended: true }));

    // CORS đơn giản cho mobile/web
    app.use((req, res, next) => {
      res.header('Access-Control-Allow-Origin', '*');
      res.header('Access-Control-Allow-Methods', 'GET,POST,PUT,PATCH,DELETE,OPTIONS');
      res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
      if (req.method === 'OPTIONS') {
        return res.sendStatus(204);
      }
      next();
    });

    // Serve static files từ thư mục uploads
    app.use('/uploads', express.static('uploads'));

    // Routes API
    const usersRoutes = require('./routes/users');
    const productsRoutes = require('./routes/products');
    const categoriesRoutes = require('./routes/categories');
    const ordersRoutes = require('./routes/orders');
    const reviewsRoutes = require('./routes/reviews');
    const favoritesRoutes = require('./routes/favorites');
    const vouchersRoutes = require('./routes/vouchers');
    const uploadRoutes = require('./routes/upload');
    const notificationsRoutes = require('./routes/notifications');
    const cartRoutes = require('./routes/cart');
    const statisticsRoutes = require('./routes/statistics');
    const warehouseRoutes = require('./routes/warehouse');

    app.use('/api/users', usersRoutes);
    app.use('/api/products', productsRoutes);
    app.use('/api/categories', categoriesRoutes);
    app.use('/api/orders', ordersRoutes);
    app.use('/api/reviews', reviewsRoutes);
    app.use('/api/favorites', favoritesRoutes);
    app.use('/api/vouchers', vouchersRoutes);
    app.use('/api/upload', uploadRoutes);
    app.use('/api/notifications', notificationsRoutes);
    app.use('/api/cart', cartRoutes);
    app.use('/api/statistics', statisticsRoutes);
    app.use('/api/warehouse', warehouseRoutes);

    // Route chính
    app.get('/', (req, res) => {
      res.json({ 
        message: 'Server đang chạy!',
        database: 'duan1',
        collections: ['users', 'products', 'categories', 'orders', 'reviews', 'favorites', 'vouchers', 'notifications', 'carts'],
        apiEndpoints: {
          users: '/api/users',
          products: '/api/products',
          categories: '/api/categories',
          orders: '/api/orders',
          reviews: '/api/reviews',
          favorites: '/api/favorites',
          vouchers: '/api/vouchers',
          upload: '/api/upload',
          notifications: '/api/notifications',
          cart: '/api/cart',
          statistics: '/api/statistics',
          warehouse: '/api/warehouse'
        }
      });
    });

    const PORT = process.env.PORT || 3000;
    const HOST = process.env.HOST || '0.0.0.0'; // Lắng nghe trên tất cả interfaces

    app.listen(PORT, HOST, () => {
      console.log(`✅ Server đang chạy trên http://${HOST === '0.0.0.0' ? 'localhost' : HOST}:${PORT}`);
      console.log(`📱 Android emulator có thể kết nối qua: http://10.0.2.2:${PORT}`);
      console.log(`🌐 Network access: http://localhost:${PORT}`);
    });
  } catch (error) {
    console.error('❌ Không thể khởi động server:', error.message);
    process.exit(1);
  }
};

// <<<<<<< HEAD
// Serve static files từ thư mục uploads
app.use('/uploads', express.static('uploads'));

// Routes API (tạm thời để trống, sẽ thêm logic sau)
const usersRoutes = require('./routes/users');
const productsRoutes = require('./routes/products');
const categoriesRoutes = require('./routes/categories');
const ordersRoutes = require('./routes/orders');
const reviewsRoutes = require('./routes/reviews');
const favoritesRoutes = require('./routes/favorites');
const cartRoutes = require('./routes/cart');
const vouchersRoutes = require('./routes/vouchers');
const uploadRoutes = require('./routes/upload');

app.use('/api/users', usersRoutes);
app.use('/api/products', productsRoutes);
app.use('/api/categories', categoriesRoutes);
app.use('/api/orders', ordersRoutes);
app.use('/api/reviews', reviewsRoutes);
app.use('/api/favorites', favoritesRoutes);
app.use('/api/vouchers', vouchersRoutes);
app.use('/api/upload', uploadRoutes);
app.use('/api/v1/cart', cartRoutes);

// Route chính
app.get('/', (req, res) => {
  res.json({ 
    message: 'Server đang chạy!',
    database: 'duan1',
    collections: ['users', 'products', 'categories', 'orders', 'reviews', 'favorites', 'vouchers'],
    apiEndpoints: {
      users: '/api/users',
      products: '/api/products',
      categories: '/api/categories',
      orders: '/api/orders',
      reviews: '/api/reviews',
      favorites: '/api/favorites',
      vouchers: '/api/vouchers',
      upload: '/api/upload'
    }
  });
});

const PORT = process.env.PORT || 3000;
const HOST = process.env.HOST || '0.0.0.0'; // Lắng nghe trên tất cả interfaces

app.listen(PORT, HOST, () => {
  console.log(`✅ Server đang chạy trên http://${HOST === '0.0.0.0' ? 'localhost' : HOST}:${PORT}`);
  console.log(`📱 Android emulator có thể kết nối qua: http://10.0.2.2:${PORT}`);
  console.log(`🌐 Network access: http://localhost:${PORT}`);
});
// =======
// startServer();

// >>>>>>> 3f8aaddc73892d73dcd74905b2ff593a3a621411
