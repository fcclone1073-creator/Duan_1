# Hướng dẫn chạy Server

## ⚠️ QUAN TRỌNG: Phải chạy từ thư mục `sever`

Server Node.js nằm trong thư mục `sever`, bạn **PHẢI** chuyển vào thư mục đó trước khi chạy lệnh.

## Cách 1: Chạy thủ công

### Windows (PowerShell):
```powershell
cd sever
npm start
```

### Windows (Command Prompt):
```cmd
cd sever
npm start
```

## Cách 2: Sử dụng script có sẵn

### Windows PowerShell:
```powershell
cd sever
.\start-server.ps1
```

## Cách 3: Development mode (tự động restart khi có thay đổi)

```powershell
cd sever
npm run dev
```

## Kiểm tra Server

Sau khi chạy, server sẽ chạy trên:
- **Local**: http://localhost:3000
- **Android Emulator**: http://10.0.2.2:3000
- **Network**: http://[IP-của-bạn]:3000

## Lỗi thường gặp

### ❌ Lỗi: "Missing script: 'start'"
**Nguyên nhân**: Bạn đang ở thư mục sai (thư mục gốc `Duan_1` thay vì `sever`)

**Giải pháp**: 
```powershell
cd sever
npm start
```

### ❌ Lỗi: "Cannot find module"
**Nguyên nhân**: Chưa cài đặt dependencies

**Giải pháp**:
```powershell
cd sever
npm install
npm start
```

### ❌ Lỗi: "MongoDB connection failed"
**Nguyên nhân**: MongoDB chưa được khởi động hoặc connection string sai

**Giải pháp**: 
1. Kiểm tra MongoDB đã chạy chưa
2. Tạo file `.env` trong thư mục `sever` với nội dung:
   ```
   MONGODB_URI=mongodb://localhost:27017/duan1
   ```

