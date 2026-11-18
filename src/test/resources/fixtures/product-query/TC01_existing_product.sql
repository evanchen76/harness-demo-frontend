-- product-query TC01：查詢存在的商品
-- Given 一個啟用的分類，底下有一個草稿商品
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, status)
VALUES (100, 'TEST-Q-001', '測試商品', 100, 990.00, 500.00, 10, 'https://img.example.com/test-q-001.jpg', 'DRAFT');
