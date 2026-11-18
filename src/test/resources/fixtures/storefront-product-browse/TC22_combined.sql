-- storefront-product-browse TC22：篩選、搜尋、排序、分頁組合
-- Given 分類 100、200；100 下 101～105 名稱含「杯」價 50/40/30/20/10，106「盤子」；200 下 201「杯」
INSERT INTO category (id, name, enabled) VALUES (100, '分類甲', 1);
INSERT INTO category (id, name, enabled) VALUES (200, '分類乙', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '杯1', 100, 50.00, 50.00, 20, 'https://img.example.com/101.jpg', '描述101', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (102, 'TEST-S-102', '杯2', 100, 40.00, 50.00, 20, 'https://img.example.com/102.jpg', '描述102', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (103, 'TEST-S-103', '杯3', 100, 30.00, 50.00, 20, 'https://img.example.com/103.jpg', '描述103', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (104, 'TEST-S-104', '杯4', 100, 20.00, 50.00, 20, 'https://img.example.com/104.jpg', '描述104', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (105, 'TEST-S-105', '杯5', 100, 10.00, 50.00, 20, 'https://img.example.com/105.jpg', '描述105', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (106, 'TEST-S-106', '盤子', 100, 100.00, 50.00, 20, 'https://img.example.com/106.jpg', '描述106', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (201, 'TEST-S-201', '杯', 200, 100.00, 50.00, 20, 'https://img.example.com/201.jpg', '描述201', 'ON_SHELF', '2026-11-01 10:00:00');
