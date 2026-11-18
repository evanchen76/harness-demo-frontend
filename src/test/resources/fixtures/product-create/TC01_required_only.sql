-- product-create TC01：只填必填欄位新增成功
-- Given 啟用中的分類（id 100）；DB 沒有 SKU-NEW
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
