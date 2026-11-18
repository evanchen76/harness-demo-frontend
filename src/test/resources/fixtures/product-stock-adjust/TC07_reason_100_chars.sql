-- product-stock-adjust TC07：原因剛好 100 個中文字
-- Given 啟用中的分類；一個 ON_SHELF 商品，庫存 50，on_shelf_time 2026-01-01 10:00:00
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-ADJ-001', '測試商品', 100, 1290.00, 800.00, 50, 'https://img.example.com/test-adj-001.jpg', '測試描述', 'ON_SHELF', '2026-01-01 10:00:00');
