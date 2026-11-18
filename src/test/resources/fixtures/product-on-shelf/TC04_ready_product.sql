-- product-on-shelf TC04：缺少操作人
-- Given 啟用中的分類；一個可上架的 DRAFT 商品
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-001', '測試商品', 100, 1290.00, 800.00, 50, 'https://img.example.com/test-s-001.jpg', '測試描述', 'DRAFT', NULL);
