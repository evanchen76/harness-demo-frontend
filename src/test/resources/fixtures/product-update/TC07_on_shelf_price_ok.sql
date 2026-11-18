-- product-update TC07：ON_SHELF 商品改價格，仍符合上架條件
-- Given 一個 ON_SHELF 基準商品（id 100），價格 1290、成本 800
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'SKU-BASE', '舊名稱', 100, 1290.00, 800.00, 50, 'https://img.example.com/base.jpg', '舊描述', 'ON_SHELF', '2026-01-01 10:00:00');
