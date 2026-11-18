-- product-on-shelf TC12：庫存為 0
-- Given 啟用中的分類；DRAFT 商品庫存 0
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-001', '測試商品', 100, 1290.00, 800.00, 0, 'https://img.example.com/test-s-001.jpg', '測試描述', 'DRAFT', NULL);
