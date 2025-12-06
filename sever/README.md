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

## Hướng dẫn test API Products

Các endpoint liên quan nằm ở `routes/products.js`:
- Danh sách: `GET /api/products` (routes/products.js:12)
- Chi tiết: `GET /api/products/:id` (routes/products.js:24)
- Thêm: `POST /api/products` (routes/products.js:37)
- Cập nhật: `PUT /api/products/:id` (routes/products.js:47)
- Xóa: `DELETE /api/products/:id` (routes/products.js:66)

### 1) Lấy danh sách sản phẩm

PowerShell:
```powershell
iwr -UseBasicParsing http://localhost:3000/api/products
```

curl:
```bash
curl http://localhost:3000/api/products
```

### 2) Lấy chi tiết sản phẩm

PowerShell:
```powershell
iwr -UseBasicParsing http://localhost:3000/api/products/<productId>
```

curl:
```bash
curl http://localhost:3000/api/products/<productId>
```

### 3) Thêm sản phẩm

Lưu ý: Trường `category` cần một `ObjectId` của danh mục có sẵn. Có thể lấy ID danh mục bằng:
```powershell
iwr -UseBasicParsing http://localhost:3000/api/categories
```

Ví dụ payload:
```json
{
  "name": "Áo phông",
  "description": "Áo phông cotton",
  "price": 99000,
  "category": "<categoryId>",
  "stock": 100,
  "image": "https://example.com/aophong.jpg"
}
```

PowerShell:
```powershell
$body = {
  name = "Áo phông"
  description = "Áo phông cotton"
  price = 99000
  category = "<categoryId>"
  stock = 100
  image = "https://example.com/aophong.jpg"
} | ConvertTo-Json
iwr -UseBasicParsing http://localhost:3000/api/products -Method Post -ContentType 'application/json' -Body $body
```

curl:
```bash
curl -X POST http://localhost:3000/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Áo phông",
    "description": "Áo phông cotton",
    "price": 99000,
    "category": "<categoryId>",
    "stock": 100,
    "image": "https://example.com/aophong.jpg"
  }'
```

### 4) Cập nhật sản phẩm

Ví dụ cập nhật giá và tồn kho:
```json
{
  "price": 109000,
  "stock": 120
}
```

PowerShell:
```powershell
$update = @{ price = 109000; stock = 120 } | ConvertTo-Json
iwr -UseBasicParsing http://localhost:3000/api/products/<productId> -Method Put -ContentType 'application/json' -Body $update
```

curl:
```bash
curl -X PUT http://localhost:3000/api/products/<productId> \
  -H "Content-Type: application/json" \
  -d '{ "price": 109000, "stock": 120 }'
```

### 5) Xóa sản phẩm

PowerShell:
```powershell
iwr -UseBasicParsing http://localhost:3000/api/products/<productId> -Method Delete
```

curl:
```bash
curl -X DELETE http://localhost:3000/api/products/<productId>
```

### Định dạng phản hồi

Tất cả endpoint trả về theo cấu trúc:
```json
{
  "success": true,
  "message": "...",
  "data": { }
}
```

## Hướng dẫn test API Products (Postman)

Các endpoint ở `routes/products.js`:
- `GET /api/products` (routes/products.js:12)
- `GET /api/products/:id` (routes/products.js:24)
- `POST /api/products` (routes/products.js:37)
- `PUT /api/products/:id` (routes/products.js:47)
- `DELETE /api/products/:id` (routes/products.js:66)

### 1) Danh sách sản phẩm
- Method: `GET`
- URL: `http://localhost:3000/api/products`
- Headers: không bắt buộc
- Kết quả: mảng sản phẩm đã populate `category`

### 2) Chi tiết sản phẩm
- Method: `GET`
- URL: `http://localhost:3000/api/products/<productId>`
- Headers: không bắt buộc

### 3) Thêm sản phẩm
- Method: `POST`
- URL: `http://localhost:3000/api/products`
- Headers: `Content-Type: application/json`
- Body (JSON):
```json
{
  "name": "Áo phông",
  "description": "Áo phông cotton",
  "price": 99000,
  "category": "<categoryId>",
  "stock": 100,
  "image": "https://example.com/aophong.jpg"
}
```
- Gợi ý lấy `categoryId`: gọi `GET http://localhost:3000/api/categories`

### 4) Cập nhật sản phẩm
- Method: `PUT`
- URL: `http://localhost:3000/api/products/<productId>`
- Headers: `Content-Type: application/json`
- Body (JSON ví dụ):
```json
{
  "price": 109000,
  "stock": 120
}
```

### 5) Xóa sản phẩm
- Method: `DELETE`
- URL: `http://localhost:3000/api/products/<productId>`
- Headers: không bắt buộc

### Lưu ý khi test
- `category` phải là `ObjectId` hợp lệ và tồn tại.
- Khi `productId` không đúng sẽ nhận `404 Không tìm thấy sản phẩm`.
- Các trường `price` ≥ 0, `stock` ≥ 0.

## Hướng dẫn test API Giỏ Hàng (Postman)

Các API được triển khai tại `routes/cart.js` và đăng ký ở `server.js:39–45` (mount `'/api/v1/cart'`).

### 1) Lấy Giỏ hàng hiện tại

- Method: `GET`
- URL: `http://localhost:3000/api/v1/cart?userId=<userId>`
- Headers: không bắt buộc
- Kết quả: thông tin `GioHang` và mảng `items` đã populate `Product`
- Tham chiếu code: `routes/cart.js:34`

### 2) Thêm/Tạo Item vào Giỏ hàng

- Method: `POST`
- URL: `http://localhost:3000/api/v1/cart/items`
- Headers: `Content-Type: application/json`
- Body (JSON example):
```json
{
  "userId": "<userId>",
  "productId": "<productId>",
  "soLuong": 2
}
```
- Lưu ý: backend tự tạo `GioHang` nếu chưa tồn tại; nếu item đã có thì tăng `soLuong`
- Tham chiếu code: `routes/cart.js:60`

### 3) Cập nhật Item (đổi số lượng)

- Method: `PUT`
- URL: `http://localhost:3000/api/v1/cart/items/<productId>`
- Headers: `Content-Type: application/json`
- Body (JSON example):
```json
{
  "userId": "<userId>",
  "soLuong": 5
}
```
- Lưu ý: nếu `soLuong = 0` hệ thống sẽ xóa item khỏi giỏ
- Tham chiếu code: `routes/cart.js:88`

### 4) Xóa Item khỏi Giỏ hàng

- Method: `DELETE`
- URL: `http://localhost:3000/api/v1/cart/items/<productId>?userId=<userId>`
- Headers: không bắt buộc
- Tham chiếu code: `routes/cart.js:110`

### 5) Xóa toàn bộ Giỏ hàng

- Method: `DELETE`
- URL: `http://localhost:3000/api/v1/cart?userId=<userId>`
- Headers: không bắt buộc
- Tham chiếu code: `routes/cart.js:46`

### Mẹo khi test

- `<userId>` cần là `ObjectId` hợp lệ của `User`
- `<productId>` lấy từ `GET http://localhost:3000/api/products` hoặc từ dữ liệu bạn đã tạo
- Luôn đặt `Content-Type: application/json` cho `POST/PUT`
- Sau mỗi thao tác, `tongGia` được tính lại tự động dựa trên `soLuong * donGia`

## Hướng dẫn test API Thống Kê (Top) trên Postman

Các endpoint nằm trong `routes/orders.js`:
- Top sản phẩm: `GET /api/orders/top-products` (routes/orders.js:89)
- Top khách hàng: `GET /api/orders/top-customers` (routes/orders.js:148)

### 1) Top sản phẩm bán chạy

- Method: `GET`
- URL cơ bản: `http://localhost:3000/api/orders/top-products`
- Query hỗ trợ:
  - `limit`: số lượng kết quả, ví dụ `5`
  - `status`: trạng thái đơn, ví dụ `delivered` (mặc định)
  - `start`, `end`: lọc theo thời gian `createdAt` (ISO 8601)
- Ví dụ URL hoàn chỉnh:
  - `http://localhost:3000/api/orders/top-products?limit=5&status=delivered&start=2025-01-01T00:00:00.000Z&end=2025-12-31T23:59:59.999Z`
- Kết quả: mảng `items` gồm `{ product, totalQty, revenue, orderCount }`

### 2) Top khách hàng

- Method: `GET`
- URL cơ bản: `http://localhost:3000/api/orders/top-customers`
- Query hỗ trợ:
  - `limit`: số lượng kết quả, ví dụ `5`
  - `status`: trạng thái đơn, ví dụ `delivered`
  - `start`, `end`: lọc theo thời gian `createdAt`
- Ví dụ URL hoàn chỉnh:
  - `http://localhost:3000/api/orders/top-customers?limit=5&status=delivered&start=2025-01-01T00:00:00.000Z&end=2025-12-31T23:59:59.999Z`
- Kết quả: mảng `items` gồm `{ user, totalItems, revenue, orderCount }`

### Lưu ý khi test

- Nếu dữ liệu chưa có đơn ở trạng thái `delivered`, kết quả có thể rỗng. Bạn có thể bỏ `status` hoặc dùng `pending` để kiểm tra.
- Thời gian `start`/`end` nên dùng định dạng ISO 8601 hợp lệ (ví dụ `2025-11-01T00:00:00.000Z`).

## Hướng dẫn test API Users (Postman)

Các endpoint ở `routes/users.js`:
- Đăng ký: `POST /api/users/register` (routes/users.js:37)
- Đăng nhập: `POST /api/users/login` (routes/users.js:79)
- Danh sách: `GET /api/users/list` (routes/users.js:109)
- Chi tiết: `GET /api/users/detail/:id` (routes/users.js:149)
- Cập nhật: `PUT /api/users/:id` (routes/users.js:167)
- Xóa: `DELETE /api/users/:id` (routes/users.js:191)

### 1) Đăng ký
- Method: `POST`
- URL: `http://localhost:3000/api/users/register`
- Headers: `Content-Type: application/json`
- Body JSON:
```json
{
  "name": "Nguyen Van A",
  "email": "a@example.com",
  "password": "123456"
}
```
- Lưu ý: `password` tối thiểu 6 ký tự; email không được trùng.

### 2) Đăng nhập
- Method: `POST`
- URL: `http://localhost:3000/api/users/login`
- Headers: `Content-Type: application/json`
- Body JSON:
```json
{
  "email": "a@example.com",
  "password": "123456"
}
```
- Kết quả: trả `token` (simple random) và `user` đã ẩn mật khẩu.

### 3) Danh sách người dùng (có phân trang/tìm kiếm/sắp xếp)
- Method: `GET`
- URL cơ bản: `http://localhost:3000/api/users/list`
- Query hỗ trợ:
  - `page` (mặc định `1`)
  - `limit` (mặc định `10`)
  - `search` (tìm theo `name`/`email`)
  - `sortBy` (mặc định `createdAt`)
  - `order` (`asc`/`desc`, mặc định `desc`)
- Ví dụ: `http://localhost:3000/api/users/list?page=1&limit=20&search=a&sortBy=name&order=asc`

### 4) Chi tiết người dùng
- Method: `GET`
- URL: `http://localhost:3000/api/users/detail/<userId>`
- Lưu ý: nếu `userId` không phải `ObjectId` hợp lệ sẽ trả `400`.

### 5) Cập nhật người dùng
- Method: `PUT`
- URL: `http://localhost:3000/api/users/<userId>`
- Headers: `Content-Type: application/json`
- Body JSON ví dụ:
```json
{
  "name": "Nguyen Van B",
  "role": "admin"
}
```
- Lưu ý: nếu gửi `password`, hệ thống sẽ tự hash.

### 6) Xóa người dùng
- Method: `DELETE`
- URL: `http://localhost:3000/api/users/<userId>`

### Lưu ý khi test Users
- `email` được lưu dạng lowercase.
- Trả về theo cấu trúc chung `success`, `message`, `data`.

## Hướng dẫn test API Tạo Đơn Hàng (Postman)

- Endpoint: `POST /api/orders` (routes/orders.js:207)
- Headers: `Content-Type: application/json`

### Chuẩn bị ID cần thiết

- Lấy `userId`:
  - `GET http://localhost:3000/api/users/list?limit=1` hoặc `GET http://localhost:3000/api/users`
- Lấy `productId`:
  - `GET http://localhost:3000/api/products`

### Body mẫu (JSON)

```json
{
  "user": "<userId>",
  "items": [
    { "product": "<productId1>", "quantity": 2, "price": 16000000 },
    { "product": "<productId2>", "quantity": 1, "price": 500000 }
  ],
  "shippingAddress": "Số 1 Đường ABC, Quận XYZ",
  "status": "pending"
}
```

### Các bước trong Postman

- Chọn method `POST`
- URL: `http://localhost:3000/api/orders`
- Tab Headers: thêm `Content-Type: application/json`
- Tab Body: chọn `raw` và `JSON`, dán nội dung JSON ở trên
- Nhấn `Send`

### Xác minh kết quả

- Phản hồi trả về `success=true` cùng đơn hàng vừa tạo (đã populate ở các API khác).
- Ghi lại `id` đơn hàng, gọi: `GET http://localhost:3000/api/orders/detail/<orderId>` để xem chi tiết.

### Lỗi thường gặp

- `400 Thiếu thông tin đơn hàng`: thiếu `user`, `items`, hoặc `shippingAddress`.
- `404 Không tìm thấy sản phẩm`: một `productId` trong `items` không tồn tại.
- `quantity` phải ≥ 1, `price` ≥ 0.
