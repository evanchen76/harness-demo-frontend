-- product-on-shelf TC13：分類不存在
-- Given 沒有分類；DRAFT 商品 category_id 指向不存在的 999

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-001', '測試商品', 999, 1290.00, 800.00, 50, 'https://img.example.com/test-s-001.jpg', '測試描述', 'DRAFT', NULL);
