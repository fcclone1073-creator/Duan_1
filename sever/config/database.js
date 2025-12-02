const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const mongoURI = process.env.MONGODB_URI || 'mongodb://localhost:27017/duan1';
    
    // Cấu hình kết nối với các options phù hợp cho Mongoose 8
    const options = {
      serverSelectionTimeoutMS: 5000, // Timeout sau 5 giây nếu không kết nối được
      socketTimeoutMS: 45000, // Timeout cho socket
    };

    const conn = await mongoose.connect(mongoURI, options);

    console.log(`✅ Kết nối MongoDB thành công`);
    console.log(`📊 Database: ${conn.connection.name}`);
    console.log(`🔗 Host: ${conn.connection.host}`);
    console.log(`🔌 Port: ${conn.connection.port}`);
    
    // Xử lý các sự kiện kết nối
    mongoose.connection.on('error', (err) => {
      console.error(`❌ MongoDB connection error: ${err.message}`);
    });

    mongoose.connection.on('disconnected', () => {
      console.warn('⚠️  MongoDB đã ngắt kết nối');
    });

    mongoose.connection.on('reconnected', () => {
      console.log('✅ MongoDB đã kết nối lại');
    });

    // Xử lý khi ứng dụng tắt
    process.on('SIGINT', async () => {
      await mongoose.connection.close();
      console.log('MongoDB connection closed through app termination');
      process.exit(0);
    });

  } catch (error) {
    console.error(`❌ Kết nối MongoDB thất bại`);
    console.error(`Error: ${error.message}`);
    
    // Thông báo chi tiết hơn về lỗi
    if (error.name === 'MongoServerSelectionError') {
      console.error('💡 Gợi ý: Kiểm tra xem MongoDB đã được khởi động chưa?');
      console.error('   - Nếu dùng MongoDB local: Chạy lệnh "mongod" hoặc khởi động MongoDB service');
      console.error('   - Nếu dùng MongoDB Atlas: Kiểm tra connection string và network access');
    } else if (error.name === 'MongoParseError') {
      console.error('💡 Gợi ý: Kiểm tra lại MONGODB_URI trong file .env');
    }
    
    process.exit(1);
  }
};

module.exports = connectDB;

