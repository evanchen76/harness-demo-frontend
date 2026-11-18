-- product-update TC02：DRAFT 商品一次改全部可編輯欄位
-- Given 一個 DRAFT 基準商品（id 100）；另有啟用中的分類 id 102
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1), (102, '另一個啟用分類', 1);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (100, 'SKU-BASE', '舊名稱', 100, 1290.00, 800.00, 50, 'https://img.example.com/base.jpg', '舊描述', 'DRAFT', NULL);
