-- product-stock-adjust TC14：商品不存在
-- Given DB 沒有 id 999 的商品（只放一個不相關的分類，讓 db-load 有資料可載）
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
