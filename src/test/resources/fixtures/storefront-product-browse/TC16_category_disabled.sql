-- storefront-product-browse TC16：篩選的分類已停用
-- Given 停用的分類 200，下一個 ON_SHELF 商品
INSERT INTO category (id, name, enabled) VALUES (200, '停用分類', 0);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (201, 'TEST-S-201', '停用分類商品', 200, 100.00, 50.00, 20, 'https://img.example.com/201.jpg', '描述201', 'ON_SHELF', '2026-11-01 10:00:00');
