-- storefront-product-browse TC11：依最新上架排序
-- Given 四個 ON_SHELF：110（11-01）、120（11-03）、130（11-02）、140（11-03）
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (110, 'TEST-S-110', '商品110', 100, 100.00, 50.00, 20, 'https://img.example.com/110.jpg', '描述110', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (120, 'TEST-S-120', '商品120', 100, 100.00, 50.00, 20, 'https://img.example.com/120.jpg', '描述120', 'ON_SHELF', '2026-11-03 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (130, 'TEST-S-130', '商品130', 100, 100.00, 50.00, 20, 'https://img.example.com/130.jpg', '描述130', 'ON_SHELF', '2026-11-02 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (140, 'TEST-S-140', '商品140', 100, 100.00, 50.00, 20, 'https://img.example.com/140.jpg', '描述140', 'ON_SHELF', '2026-11-03 10:00:00');
