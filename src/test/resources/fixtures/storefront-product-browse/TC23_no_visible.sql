-- storefront-product-browse TC23：沒有任何可見商品
-- Given 有分類，只有一個 DRAFT 商品
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '草稿', 100, 100.00, 50.00, 20, 'https://img.example.com/101.jpg', '描述101', 'DRAFT', NULL);
