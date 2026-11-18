-- storefront-product-browse TC26：詳情，草稿找不到
-- Given 一個 DRAFT 商品 100
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'TEST-S-100', '草稿', 100, 100.00, 50.00, 20, 'https://img.example.com/100.jpg', '描述100', 'DRAFT', NULL);
