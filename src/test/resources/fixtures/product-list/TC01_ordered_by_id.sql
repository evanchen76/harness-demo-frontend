-- product-list TC01：依 id 由小到大列出全部商品
-- Given 啟用中的分類；三個 ON_SHELF 商品，以 300、100、200 的順序寫入
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time, update_time)
VALUES (300, 'TEST-L-300', '商品三', 100, 300.00, 100.00, 30, 'https://img.example.com/300.jpg', '描述三', 'ON_SHELF', '2026-03-03 10:00:00', '2026-03-04 11:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time, update_time)
VALUES (100, 'TEST-L-100', '商品一', 100, 100.50, 50.00, 10, 'https://img.example.com/100.jpg', '描述一', 'ON_SHELF', '2026-01-01 10:00:00', '2026-01-02 11:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time, update_time)
VALUES (200, 'TEST-L-200', '商品二', 100, 200.00, 80.00, 20, 'https://img.example.com/200.jpg', '描述二', 'ON_SHELF', '2026-02-02 10:00:00', '2026-02-03 11:00:00');
