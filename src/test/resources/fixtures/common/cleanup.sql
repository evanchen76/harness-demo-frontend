-- 每個測試開始前執行：清空所有資料表，讓每個案例只看得到自己的 fixture
-- 用 DELETE 而不是 TRUNCATE：app_user 只有 DML 權限（TRUNCATE 需要 DROP 權限）
DELETE FROM product_stock_log;
DELETE FROM product;
DELETE FROM category;
