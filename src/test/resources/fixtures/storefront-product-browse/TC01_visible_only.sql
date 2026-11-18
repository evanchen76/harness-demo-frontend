-- storefront-product-browse TC01：列表只含上架中且分類啟用的商品
-- Given 分類 100 啟用、200 停用；100 下 ON_SHELF 兩個、DRAFT、OFF_SHELF、SOLD_OUT 各一；200 下 ON_SHELF 一個
INSERT INTO category (id, name, enabled) VALUES (100, '分類甲', 1);
INSERT INTO category (id, name, enabled) VALUES (200, '分類乙', 0);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '上架一', 100, 100.00, 50.00, 20, 'https://img.example.com/101.jpg', '描述101', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (102, 'TEST-S-102', '上架二', 100, 100.00, 50.00, 20, 'https://img.example.com/102.jpg', '描述102', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (103, 'TEST-S-103', '草稿', 100, 100.00, 50.00, 20, 'https://img.example.com/103.jpg', '描述103', 'DRAFT', NULL);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (104, 'TEST-S-104', '已下架', 100, 100.00, 50.00, 20, 'https://img.example.com/104.jpg', '描述104', 'OFF_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (105, 'TEST-S-105', '已售完', 100, 100.00, 50.00, 0, 'https://img.example.com/105.jpg', '描述105', 'SOLD_OUT', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (106, 'TEST-S-106', '停用分類商品', 200, 100.00, 50.00, 20, 'https://img.example.com/106.jpg', '描述106', 'ON_SHELF', '2026-11-01 10:00:00');
