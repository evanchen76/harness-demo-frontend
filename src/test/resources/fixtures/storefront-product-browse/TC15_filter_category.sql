-- storefront-product-browse TC15：依分類篩選
-- Given 分類 100、200 皆啟用；100 下兩個、200 下三個 ON_SHELF
INSERT INTO category (id, name, enabled) VALUES (100, '分類甲', 1);
INSERT INTO category (id, name, enabled) VALUES (200, '分類乙', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '甲一', 100, 100.00, 50.00, 20, 'https://img.example.com/101.jpg', '描述101', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (102, 'TEST-S-102', '甲二', 100, 100.00, 50.00, 20, 'https://img.example.com/102.jpg', '描述102', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (201, 'TEST-S-201', '乙一', 200, 100.00, 50.00, 20, 'https://img.example.com/201.jpg', '描述201', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (202, 'TEST-S-202', '乙二', 200, 100.00, 50.00, 20, 'https://img.example.com/202.jpg', '描述202', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (203, 'TEST-S-203', '乙三', 200, 100.00, 50.00, 20, 'https://img.example.com/203.jpg', '描述203', 'ON_SHELF', '2026-11-01 10:00:00');
