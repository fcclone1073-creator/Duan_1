# Server API - Duan1

## Cài đặt

1. Cài đặt dependencies:
```bash
npm install
```

2. Cấu hình MongoDB:
   - Tạo file `.env` trong thư mục `sever` (nếu chưa có)
   - Thêm dòng: `MONGODB_URI=mongodb://localhost:27017/duan1`
   - Hoặc sử dụng MongoDB Atlas: `MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/duan1`

## Chạy Server

### Development mode:
```bash
npm run dev
```

### Production mode:
```bash
npm start
```

## Kiểm tra Server

Sau khi chạy server, mở trình duyệt và truy cập:
- http://localhost:3000

Bạn sẽ thấy thông báo server đang chạy.

## API Endpoints

### Users: `/api/users`
  - `POST /api/users/register` - Đăng ký tài khoản mới
  - `POST /api/users/login` - Đăng nhập
  - `GET /api/users` - Danh sách tất cả users
  - `GET /api/users/:id` - Chi tiết user
  - `PUT /api/users/:id` - Cập nhật user
  - `DELETE /api/users/:id` - Xóa user

### Products: `/api/products`
  - `GET /api/products` - Danh sách sản phẩm (hỗ trợ tìm kiếm và lọc)
    - Query parameters:
      - `search` - Tìm kiếm theo tên sản phẩm
      - `category` - Lọc theo ID danh mục
      - `minPrice` - Giá tối thiểu
      - `maxPrice` - Giá tối đa
      - `inStock` - Còn hàng (true/false)
      - `sortBy` - Sắp xếp: `price_asc`, `price_desc`, `name_asc`, `name_desc`, `newest`, `oldest`
      - `page` - Số trang (mặc định: 1)
      - `limit` - Số lượng mỗi trang (mặc định: 20)
    - Ví dụ: `/api/products?search=iphone&minPrice=1000000&maxPrice=5000000&sortBy=price_asc&page=1&limit=10`
  - `GET /api/products/:id` - Chi tiết sản phẩm
  - `POST /api/products` - Thêm sản phẩm mới
  - `PUT /api/products/:id` - Cập nhật sản phẩm
  - `DELETE /api/products/:id` - Xóa sản phẩm

### Categories: `/api/categories`
  - `GET /api/categories` - Danh sách danh mục
  - `GET /api/categories/:id` - Chi tiết danh mục
  - `POST /api/categories` - Thêm danh mục
  - `PUT /api/categories/:id` - Cập nhật danh mục
  - `DELETE /api/categories/:id` - Xóa danh mục

### Orders: `/api/orders`
  - `GET /api/orders` - Danh sách đơn hàng
  - `GET /api/orders/user/:userId` - Đơn hàng theo user
  - `GET /api/orders/:id` - Chi tiết đơn hàng
  - `POST /api/orders` - Tạo đơn hàng mới
  - `PUT /api/orders/:id` - Cập nhật đơn hàng
  - `DELETE /api/orders/:id` - Xóa đơn hàng

### Reviews: `/api/reviews`
  - `GET /api/reviews` - Danh sách đánh giá
  - `GET /api/reviews/product/:productId` - Đánh giá theo sản phẩm
  - `GET /api/reviews/:id` - Chi tiết đánh giá
  - `POST /api/reviews` - Thêm đánh giá
  - `PUT /api/reviews/:id` - Cập nhật đánh giá
  - `DELETE /api/reviews/:id` - Xóa đánh giá

### Favorites: `/api/favorites`
  - `GET /api/favorites` - Danh sách yêu thích
  - `GET /api/favorites/user/:userId` - Yêu thích theo user
  - `POST /api/favorites` - Thêm vào yêu thích
  - `DELETE /api/favorites/:id` - Xóa yêu thích theo ID
  - `DELETE /api/favorites/user/:userId/product/:productId` - Xóa yêu thích theo user và product

### Vouchers: `/api/vouchers`
  - `GET /api/vouchers` - Danh sách voucher
  - `GET /api/vouchers/active` - Voucher đang hoạt động
  - `GET /api/vouchers/code/:code` - Lấy voucher theo mã
  - `POST /api/vouchers` - Tạo voucher mới
  - `POST /api/vouchers/validate` - Kiểm tra voucher hợp lệ
  - `PUT /api/vouchers/:id` - Cập nhật voucher
  - `DELETE /api/vouchers/:id` - Xóa voucher

### Upload: `/api/upload`
  - `POST /api/upload` - Upload 1 ảnh
  - `POST /api/upload/multiple` - Upload nhiều ảnh (tối đa 10)

### Notifications: `/api/notifications`
  - `POST /api/notifications` - Gửi thông báo hệ thống (Admin)
  - `GET /api/notifications/user/:userId` - Danh sách thông báo của user
    - Query: `unreadOnly=true` - Chỉ lấy thông báo chưa đọc
  - `GET /api/notifications/user/:userId/unread-count` - Đếm thông báo chưa đọc
  - `PUT /api/notifications/:id/read` - Đánh dấu đã đọc
  - `PUT /api/notifications/user/:userId/read-all` - Đánh dấu tất cả đã đọc
  - `DELETE /api/notifications/:id` - Xóa thông báo

### Cart: `/api/cart`
  - `GET /api/cart/user/:userId` - Lấy giỏ hàng của user
  - `POST /api/cart/user/:userId/item` - Thêm sản phẩm vào giỏ hàng
  - `PUT /api/cart/user/:userId/item/:productId` - Cập nhật số lượng
  - `DELETE /api/cart/user/:userId/item/:productId` - Xóa sản phẩm khỏi giỏ hàng
  - `DELETE /api/cart/user/:userId` - Xóa toàn bộ giỏ hàng

### Statistics: `/api/statistics`
  - `GET /api/statistics/overview` - Thống kê tổng quan
  - `GET /api/statistics/revenue` - Thống kê doanh thu theo thời gian
    - Query: `startDate`, `endDate`, `groupBy` (day/month/year)
  - `GET /api/statistics/products/top-selling` - Top sản phẩm bán chạy
  - `GET /api/statistics/products/top-rated` - Top sản phẩm đánh giá cao
  - `GET /api/statistics/orders/status` - Thống kê đơn hàng theo trạng thái
  - `GET /api/statistics/categories` - Thống kê theo danh mục

### Warehouse: `/api/warehouse`
  - `GET /api/warehouse/overview` - Tổng quan kho
  - `GET /api/warehouse/products` - Danh sách sản phẩm trong kho
    - Query: `lowStock`, `outOfStock`, `category`, `sortBy`, `page`, `limit`
  - `PUT /api/warehouse/products/:id/stock` - Cập nhật tồn kho
    - Body: `{ stock, action: 'set'|'add'|'subtract', quantity }`
  - `GET /api/warehouse/products/low-stock` - Sản phẩm sắp hết hàng
  - `GET /api/warehouse/products/out-of-stock` - Sản phẩm hết hàng

## Lưu ý

- Server mặc định chạy trên port **3000**
- Android emulator kết nối qua: `http://10.0.2.2:3000`
- Thiết bị thật: sử dụng IP máy tính của bạn (ví dụ: `http://192.168.1.100:3000`)

