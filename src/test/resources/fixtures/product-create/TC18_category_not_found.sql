-- product-create TC18：分類不存在
-- Given DB 沒有 id 999 的分類（只有啟用中的 id 100）
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
