-- product-list TC03：四種狀態都以英文代碼列出
-- Given 啟用中的分類；四個商品，狀態分別為 DRAFT、ON_SHELF、OFF_SHELF、SOLD_OUT
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);

INSERT INTO product (id, sku, name, category_id, price, stock, status) VALUES (100, 'TEST-L-100', '草稿', 100, 10.00, 5, 'DRAFT');
INSERT INTO product (id, sku, name, category_id, price, stock, status) VALUES (101, 'TEST-L-101', '上架中', 100, 10.00, 5, 'ON_SHELF');
INSERT INTO product (id, sku, name, category_id, price, stock, status) VALUES (102, 'TEST-L-102', '已下架', 100, 10.00, 5, 'OFF_SHELF');
INSERT INTO product (id, sku, name, category_id, price, stock, status) VALUES (103, 'TEST-L-103', '已售完', 100, 10.00, 0, 'SOLD_OUT');
