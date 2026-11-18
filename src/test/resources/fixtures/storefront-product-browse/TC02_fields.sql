-- storefront-product-browse TC02：列表每筆只含指定欄位
-- Given 分類「家電」；一個 ON_SHELF 商品，各欄位都有值
INSERT INTO category (id, name, enabled) VALUES (100, '家電', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-100', '電鍋', 100, 199.50, 80.00, 50, 'https://img.example.com/100.jpg', '好用的電鍋', 'ON_SHELF', '2026-11-01 10:00:00');
