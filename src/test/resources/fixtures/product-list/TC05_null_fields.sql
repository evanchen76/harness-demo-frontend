-- product-list TC05：沒填的欄位回 null
-- Given 啟用中的分類；一個 DRAFT 商品，name、main_image_url、on_shelf_time 為 null
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, stock, main_image_url, status, on_shelf_time)
VALUES (100, 'TEST-L-001', NULL, 100, 99.00, 3, NULL, 'DRAFT', NULL);
