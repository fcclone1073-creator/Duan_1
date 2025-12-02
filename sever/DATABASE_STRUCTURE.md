# Cấu trúc Database - Duan1

## Database: `duan1`

Database này chứa 9 collections chính:

### 1. **users** (Người dùng)
- `_id`: ObjectId
- `name`: String (bắt buộc)
- `email`: String (bắt buộc, unique)
- `password`: String (bắt buộc, đã hash)
- `role`: String (enum: 'user', 'admin', mặc định: 'user')
- `createdAt`: Date
- `updatedAt`: Date

### 2. **products** (Sản phẩm)
- `_id`: ObjectId
- `name`: String (bắt buộc)
- `description`: String
- `price`: Number (bắt buộc, min: 0)
- `category`: ObjectId (ref: Category, bắt buộc)
- `stock`: Number (bắt buộc, min: 0, mặc định: 0)
- `image`: String
- `createdAt`: Date
- `updatedAt`: Date

### 3. **categories** (Danh mục)
- `_id`: ObjectId
- `name`: String (bắt buộc, unique)
- `description`: String
- `createdAt`: Date
- `updatedAt`: Date

### 4. **orders** (Đơn hàng)
- `_id`: ObjectId
- `user`: ObjectId (ref: User, bắt buộc)
- `items`: Array of OrderItem
  - `product`: ObjectId (ref: Product, bắt buộc)
  - `quantity`: Number (bắt buộc, min: 1)
  - `price`: Number (bắt buộc)
- `totalAmount`: Number (bắt buộc, min: 0)
- `status`: String (enum: 'pending', 'processing', 'shipped', 'delivered', 'cancelled', mặc định: 'pending')
- `shippingAddress`: String (bắt buộc)
- `createdAt`: Date
- `updatedAt`: Date

### 5. **reviews** (Đánh giá)
- `_id`: ObjectId
- `user`: ObjectId (ref: User, bắt buộc)
- `product`: ObjectId (ref: Product, bắt buộc)
- `rating`: Number (bắt buộc, min: 1, max: 5)
- `comment`: String
- `createdAt`: Date
- `updatedAt`: Date

### 6. **favorites** (Yêu thích)
- `_id`: ObjectId
- `user`: ObjectId (ref: User, bắt buộc)
- `product`: ObjectId (ref: Product, bắt buộc)
- `createdAt`: Date
- Index unique: { user: 1, product: 1 }

### 7. **vouchers** (Mã giảm giá)
- `_id`: ObjectId
- `code`: String (bắt buộc, unique, uppercase)
- `name`: String (bắt buộc)
- `description`: String
- `discountType`: String (enum: 'percentage', 'fixed', bắt buộc)
- `discountValue`: Number (bắt buộc, min: 0)
- `minPurchaseAmount`: Number (mặc định: 0, min: 0)
- `maxDiscountAmount`: Number (min: 0)
- `startDate`: Date (bắt buộc)
- `endDate`: Date (bắt buộc)
- `usageLimit`: Number (min: 0)
- `usedCount`: Number (mặc định: 0, min: 0)
- `isActive`: Boolean (mặc định: true)
- `createdAt`: Date
- `updatedAt`: Date

### 8. **notifications** (Thông báo)
- `_id`: ObjectId
- `title`: String (bắt buộc)
- `message`: String (bắt buộc)
- `type`: String (enum: 'system', 'order', 'product', 'promotion', 'other', mặc định: 'system')
- `targetUser`: ObjectId (ref: User, null = gửi cho tất cả)
- `isRead`: Boolean (mặc định: false)
- `readAt`: Date
- `createdBy`: ObjectId (ref: User, bắt buộc)
- `createdAt`: Date
- Index: { targetUser: 1, isRead: 1, createdAt: -1 }

### 9. **carts** (Giỏ hàng)
- `_id`: ObjectId
- `user`: ObjectId (ref: User, bắt buộc, unique)
- `items`: Array of CartItem
  - `product`: ObjectId (ref: Product, bắt buộc)
  - `quantity`: Number (bắt buộc, min: 1, mặc định: 1)
- `updatedAt`: Date
- Index: { user: 1 } (unique)
- `_id`: ObjectId
- `code`: String (bắt buộc, unique, uppercase)
- `name`: String (bắt buộc)
- `description`: String
- `discountType`: String (enum: 'percentage', 'fixed', bắt buộc)
- `discountValue`: Number (bắt buộc, min: 0)
- `minPurchaseAmount`: Number (mặc định: 0, min: 0)
- `maxDiscountAmount`: Number (min: 0)
- `startDate`: Date (bắt buộc)
- `endDate`: Date (bắt buộc)
- `usageLimit`: Number (min: 0)
- `usedCount`: Number (mặc định: 0, min: 0)
- `isActive`: Boolean (mặc định: true)
- `createdAt`: Date
- `updatedAt`: Date

## Mối quan hệ giữa các collections

- **User** ↔ **Order**: 1-N (một user có nhiều đơn hàng)
- **User** ↔ **Review**: 1-N (một user có nhiều đánh giá)
- **User** ↔ **Favorite**: 1-N (một user có nhiều sản phẩm yêu thích)
- **Product** ↔ **Category**: N-1 (nhiều sản phẩm thuộc một danh mục)
- **Product** ↔ **Order**: N-N (qua OrderItem)
- **Product** ↔ **Review**: 1-N (một sản phẩm có nhiều đánh giá)
- **Product** ↔ **Favorite**: N-N (qua Favorite)

## Kết nối MongoDB

Connection string mặc định: `mongodb://localhost:27017/duan1`

Có thể cấu hình trong file `.env`:
```
MONGODB_URI=mongodb://localhost:27017/duan1
```

hoặc MongoDB Atlas:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/duan1
```

