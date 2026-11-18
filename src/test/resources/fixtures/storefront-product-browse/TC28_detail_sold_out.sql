-- storefront-product-browse TC28：詳情，已售完找不到
-- Given 一個 SOLD_OUT 商品 100
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-100', '已售完', 100, 100.00, 50.00, 0, 'https://img.example.com/100.jpg', '描述100', 'SOLD_OUT', '2026-11-01 10:00:00');
