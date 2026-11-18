-- product-stock-adjust TC18：同時違反多條規則，回傳第一個
-- Given DB 沒有 id 999 的商品（只放一個不相關的分類，讓 db-load 有資料可載）
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
