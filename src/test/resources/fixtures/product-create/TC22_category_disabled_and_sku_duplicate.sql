-- product-create TC22：分類已停用又 sku 重複
-- Given 停用中的分類（id 101）；DB 已有 sku = SKU-DUP 的商品（id 100，屬於啟用中的分類 100）
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1), (101, '停用分類', 0);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status)
VALUES (100, 'SKU-DUP', '既有商品', 100, 500.00, 300.00, 10, 'https://img.example.com/dup.jpg', '既有描述', 'DRAFT');
