# Admin Panel - Nhóm 5 Shop

Trang quản trị web hiện đại để quản lý hệ thống bán hàng.

## 🚀 Tính năng

- ✅ **Đăng nhập Admin**: Xác thực người dùng với quyền admin
- ✅ **Dashboard**: Tổng quan thống kê hệ thống
- ✅ **Quản lý Sản phẩm**: Thêm, sửa, xóa sản phẩm
- ✅ **Quản lý Đơn hàng**: Xem và quản lý đơn hàng
- ✅ **Quản lý Người dùng**: Xem danh sách người dùng
- ✅ **Quản lý Danh mục**: Thêm, sửa, xóa danh mục sản phẩm
- ✅ **Giao diện đẹp**: Material Design, responsive, hiện đại

## 📋 Yêu cầu

- Backend server đang chạy tại `http://localhost:3000`
- Trình duyệt web hiện đại (Chrome, Firefox, Edge, Safari)
- Tài khoản admin đã được tạo trong database

## 🛠️ Cài đặt

1. **Mở file trong trình duyệt:**
   - Mở file `index.html` bằng trình duyệt web
   - Hoặc sử dụng Live Server extension trong VS Code

2. **Đảm bảo backend đang chạy:**
   ```bash
   cd ../sever
   npm start
   ```

3. **Tạo tài khoản admin (nếu chưa có):**
   - Đăng ký tài khoản mới qua API hoặc database
   - Cập nhật role thành `admin` trong MongoDB:
     ```javascript
     db.users.updateOne(
       { email: "admin@example.com" },
       { $set: { role: "admin" } }
     )
     ```

## 📖 Hướng dẫn sử dụng

### Đăng nhập

1. Mở file `index.html` trong trình duyệt
2. Nhập email và mật khẩu của tài khoản admin
3. Click "Đăng nhập"

### Quản lý Sản phẩm

1. Vào menu **Sản phẩm**
2. Click **Thêm sản phẩm** để tạo mới
3. Điền đầy đủ thông tin:
   - Tên sản phẩm (bắt buộc)
   - Mô tả
   - Giá (VNĐ)
   - Tồn kho
   - Danh mục
   - URL ảnh sản phẩm
   - Đánh giá (0-5)
   - Đã bán
   - Giảm giá (%)
4. Click **Thêm sản phẩm** để lưu
5. Sửa/Xóa sản phẩm bằng các nút tương ứng

### Quản lý Đơn hàng

1. Vào menu **Đơn hàng**
2. Xem danh sách tất cả đơn hàng
3. Lọc theo trạng thái (Chờ xác nhận, Đã xác nhận, Đang giao, Đã giao, Đã hủy)
4. Xem chi tiết hoặc xóa đơn hàng

### Quản lý Danh mục

1. Vào menu **Danh mục**
2. Click **Thêm danh mục** để tạo mới
3. Điền tên và mô tả danh mục
4. Sửa/Xóa danh mục bằng các nút tương ứng

### Quản lý Người dùng

1. Vào menu **Người dùng**
2. Xem danh sách tất cả người dùng
3. Xem thông tin và vai trò (Admin/User)
4. Xóa người dùng nếu cần

## 🔧 Cấu hình API

File `api.js` chứa cấu hình kết nối API. Mặc định:

```javascript
const API_BASE_URL = 'http://localhost:3000/api';
```

Nếu backend chạy ở địa chỉ khác, sửa trong file `api.js`.

## 📁 Cấu trúc File

```
admin/
├── index.html          # Trang đăng nhập
├── dashboard.html      # Trang quản trị chính
├── styles.css          # File CSS
├── api.js              # API client
├── auth.js             # Xử lý đăng nhập
├── app.js              # Logic ứng dụng chính
└── README.md           # File hướng dẫn
```

## 🎨 Giao diện

- **Màu sắc**: Teal/Cyan primary, hiện đại
- **Layout**: Sidebar navigation, responsive
- **Components**: Cards, Tables, Modals, Forms
- **Icons**: Font Awesome 6.4.0

## 🔒 Bảo mật

- Chỉ cho phép đăng nhập với role `admin`
- Token được lưu trong localStorage
- Tự động redirect về trang đăng nhập nếu chưa đăng nhập

## 🐛 Xử lý lỗi

- Kiểm tra console (F12) để xem lỗi chi tiết
- Đảm bảo backend đang chạy
- Kiểm tra CORS settings trong backend
- Kiểm tra kết nối database

## 📝 Ghi chú

- Dữ liệu được lưu trong MongoDB
- API response format: `{ success: boolean, message: string, data: any }`
- Token được lưu trong localStorage với key `admin_token`

## 🚀 Phát triển thêm

Có thể mở rộng thêm:
- Upload ảnh sản phẩm
- Biểu đồ thống kê chi tiết
- Export dữ liệu (Excel, PDF)
- Quản lý kho hàng
- Quản lý mã giảm giá
- Thông báo real-time

---

**Phát triển bởi Nhóm 5** 🎉

