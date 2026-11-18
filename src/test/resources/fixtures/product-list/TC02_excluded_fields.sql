-- product-list TC02：列表不含描述、成本價、建立時間
-- Given 啟用中的分類；一個商品，description 有值、cost_price 為 800
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status)
VALUES (100, 'TEST-L-001', '測試商品', 100, 1290.00, 800.00, 50, 'https://img.example.com/1.jpg', '商品描述', 'DRAFT');
