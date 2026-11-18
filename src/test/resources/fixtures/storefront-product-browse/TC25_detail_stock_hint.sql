-- storefront-product-browse TC25：詳情庫存提示
-- Given 兩個 ON_SHELF：101 庫存 9、102 庫存 10
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '少量', 100, 100.00, 50.00, 9, 'https://img.example.com/101.jpg', '描述101', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (102, 'TEST-S-102', '足量', 100, 100.00, 50.00, 10, 'https://img.example.com/102.jpg', '描述102', 'ON_SHELF', '2026-11-01 10:00:00');
