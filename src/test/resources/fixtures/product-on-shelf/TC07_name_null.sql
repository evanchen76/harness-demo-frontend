-- product-on-shelf TC07：缺少商品名稱
-- Given 啟用中的分類；DRAFT 商品 name 為 null
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-001', NULL, 100, 1290.00, 800.00, 50, 'https://img.example.com/test-s-001.jpg', '測試描述', 'DRAFT', NULL);
