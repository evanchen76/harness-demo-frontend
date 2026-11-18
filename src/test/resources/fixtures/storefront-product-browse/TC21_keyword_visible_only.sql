-- storefront-product-browse TC21：搜尋不會搜到不可見的商品
-- Given 分類 100 啟用、200 停用；名稱含「手機」：100 的 ON_SHELF、100 的 OFF_SHELF、200 的 ON_SHELF
INSERT INTO category (id, name, enabled) VALUES (100, '分類甲', 1);
INSERT INTO category (id, name, enabled) VALUES (200, '分類乙', 0);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '好手機', 100, 100.00, 50.00, 20, 'https://img.example.com/101.jpg', '描述101', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (102, 'TEST-S-102', '舊手機', 100, 100.00, 50.00, 20, 'https://img.example.com/102.jpg', '描述102', 'OFF_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (201, 'TEST-S-201', '停用分類手機', 200, 100.00, 50.00, 20, 'https://img.example.com/201.jpg', '描述201', 'ON_SHELF', '2026-11-01 10:00:00');
