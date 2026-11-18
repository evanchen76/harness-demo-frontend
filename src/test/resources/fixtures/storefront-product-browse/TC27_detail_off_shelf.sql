-- storefront-product-browse TC27：詳情，已下架找不到
-- Given 一個 OFF_SHELF 商品 100
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-100', '已下架', 100, 100.00, 50.00, 20, 'https://img.example.com/100.jpg', '描述100', 'OFF_SHELF', '2026-11-01 10:00:00');
