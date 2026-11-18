-- product-update TC15：DRAFT 商品換成不存在的分類
-- Given 一個 DRAFT 基準商品（id 100）；DB 沒有 id 999 的分類
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'SKU-BASE', '舊名稱', 100, 1290.00, 800.00, 50, 'https://img.example.com/base.jpg', '舊描述', 'DRAFT', NULL);
