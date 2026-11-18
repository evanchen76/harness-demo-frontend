-- product-on-shelf TC14：分類已停用
-- Given 停用中的分類；DRAFT 商品屬於該分類
INSERT INTO category (id, name, enabled) VALUES (100, '停用分類', 0);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-001', '測試商品', 100, 1290.00, 800.00, 50, 'https://img.example.com/test-s-001.jpg', '測試描述', 'DRAFT', NULL);
