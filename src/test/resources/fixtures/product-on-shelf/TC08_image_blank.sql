-- product-on-shelf TC08：主圖網址只有空白
-- Given 啟用中的分類；DRAFT 商品 main_image_url 為空白
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-001', '測試商品', 100, 1290.00, 800.00, 50, '   ', '測試描述', 'DRAFT', NULL);
