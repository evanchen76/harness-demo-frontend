-- storefront-product-browse TC03：庫存提示的邊界
-- Given 四個 ON_SHELF 商品，庫存 1、9、10、500（id 101～104）
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '商品1', 100, 100.00, 50.00, 1, 'https://img.example.com/101.jpg', '描述101', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (102, 'TEST-S-102', '商品2', 100, 100.00, 50.00, 9, 'https://img.example.com/102.jpg', '描述102', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (103, 'TEST-S-103', '商品3', 100, 100.00, 50.00, 10, 'https://img.example.com/103.jpg', '描述103', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (104, 'TEST-S-104', '商品4', 100, 100.00, 50.00, 500, 'https://img.example.com/104.jpg', '描述104', 'ON_SHELF', '2026-11-01 10:00:00');
