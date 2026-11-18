-- storefront-product-browse TC32：看不到的商品回應完全相同
-- Given 分類 100 啟用、200 停用；DRAFT 101、OFF_SHELF 102、SOLD_OUT 103、停用分類 ON_SHELF 201
INSERT INTO category (id, name, enabled) VALUES (100, '分類甲', 1);
INSERT INTO category (id, name, enabled) VALUES (200, '分類乙', 0);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '草稿', 100, 100.00, 50.00, 20, 'https://img.example.com/101.jpg', '描述101', 'DRAFT', NULL);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (102, 'TEST-S-102', '已下架', 100, 100.00, 50.00, 20, 'https://img.example.com/102.jpg', '描述102', 'OFF_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (103, 'TEST-S-103', '已售完', 100, 100.00, 50.00, 0, 'https://img.example.com/103.jpg', '描述103', 'SOLD_OUT', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (201, 'TEST-S-201', '停用分類商品', 200, 100.00, 50.00, 20, 'https://img.example.com/201.jpg', '描述201', 'ON_SHELF', '2026-11-01 10:00:00');
