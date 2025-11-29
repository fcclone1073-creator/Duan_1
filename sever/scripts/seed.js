require('dotenv').config();
const mongoose = require('mongoose');
const Category = require('../models/Category');
const Product = require('../models/Product');

const upsertCategory = async (name) => {
  const doc = await Category.findOneAndUpdate(
    { name },
    { name },
    { upsert: true, new: true, setDefaultsOnInsert: true }
  );
  return doc._id;
};

const upsertProduct = async (payload) => {
  await Product.findOneAndUpdate(
    { name: payload.name },
    { ...payload, updatedAt: new Date() },
    { upsert: true, new: true, setDefaultsOnInsert: true, runValidators: true }
  );
};

const main = async () => {
  await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/duan1');

  const dienThoaiId = await upsertCategory('Điện thoại');
  const phuKienId = await upsertCategory('Phụ kiện');

  await upsertProduct({
    name: 'iPhone 13',
    description: 'Apple A15, màn 6.1"',
    price: 18990000,
    category: dienThoaiId,
    stock: 50,
    image: 'uploads/iphone13.jpg'
  });

  await upsertProduct({
    name: 'Iphone 7plus',
    description: 'hơi cổ',
    price: 500000,
    category: dienThoaiId,
    stock: 100,
    image: 'https://cdnv2.tgdd.vn/mwg-static/common/News/1569924/9.jpg'
  });

  await upsertProduct({
    name: 'Iphone 16 pro',
    description: 'new',
    price: 16000000,
    category: dienThoaiId,
    stock: 100,
    image: 'https://cdn.phuckhangmobile.com/image/iphone-16-pro-32327j.jpg'
  });

  await upsertProduct({
    name: 'Cáp Lightning chính hãng',
    description: 'Cáp sạc Apple MFi',
    price: 250000,
    category: phuKienId,
    stock: 500,
    image: 'https://example.com/cap-lightning.jpg'
  });

  const count = await Product.countDocuments({});
  console.log('Products:', count);

  await mongoose.disconnect();
};

main().catch((e) => {
  console.error(e.message);
  process.exit(1);
});

