-- storefront-product-browse TC29：詳情，分類停用找不到
-- Given 停用的分類 200，下一個 ON_SHELF 商品 200
INSERT INTO category (id, name, enabled) VALUES (200, '停用分類', 0);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (200, 'TEST-S-200', '停用分類商品', 200, 100.00, 50.00, 20, 'https://img.example.com/200.jpg', '描述200', 'ON_SHELF', '2026-11-01 10:00:00');
