-- product-update TC05：SOLD_OUT 商品可以編輯
-- Given 一個 SOLD_OUT 基準商品（id 100），庫存 0
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'SKU-BASE', '舊名稱', 100, 1290.00, 800.00, 0, 'https://img.example.com/base.jpg', '舊描述', 'SOLD_OUT', '2026-01-01 10:00:00');
