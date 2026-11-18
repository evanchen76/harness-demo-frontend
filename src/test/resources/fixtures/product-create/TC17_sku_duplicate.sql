-- product-create TC17：sku 已存在
-- Given 啟用中的分類（id 100）；DB 已有 sku = SKU-DUP 的商品（id 100）
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status)
VALUES (100, 'SKU-DUP', '既有商品', 100, 500.00, 300.00, 10, 'https://img.example.com/dup.jpg', '既有描述', 'DRAFT');
