-- product-stock-adjust TC05：草稿商品扣到 0，狀態不變
-- Given 啟用中的分類；一個 DRAFT 商品，庫存 50，on_shelf_time null
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-ADJ-001', '測試商品', 100, 1290.00, 800.00, 50, 'https://img.example.com/test-adj-001.jpg', '測試描述', 'DRAFT', NULL);
