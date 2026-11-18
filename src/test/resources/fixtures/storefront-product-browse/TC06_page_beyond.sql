-- storefront-product-browse TC06：頁碼超出最後一頁
-- Given 25 個 ON_SHELF 商品（id 101～125，上架時間相同）
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '商品1', 100, 100.00, 50.00, 20, 'https://img.example.com/101.jpg', '描述101', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (102, 'TEST-S-102', '商品2', 100, 100.00, 50.00, 20, 'https://img.example.com/102.jpg', '描述102', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (103, 'TEST-S-103', '商品3', 100, 100.00, 50.00, 20, 'https://img.example.com/103.jpg', '描述103', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (104, 'TEST-S-104', '商品4', 100, 100.00, 50.00, 20, 'https://img.example.com/104.jpg', '描述104', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (105, 'TEST-S-105', '商品5', 100, 100.00, 50.00, 20, 'https://img.example.com/105.jpg', '描述105', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (106, 'TEST-S-106', '商品6', 100, 100.00, 50.00, 20, 'https://img.example.com/106.jpg', '描述106', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (107, 'TEST-S-107', '商品7', 100, 100.00, 50.00, 20, 'https://img.example.com/107.jpg', '描述107', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (108, 'TEST-S-108', '商品8', 100, 100.00, 50.00, 20, 'https://img.example.com/108.jpg', '描述108', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (109, 'TEST-S-109', '商品9', 100, 100.00, 50.00, 20, 'https://img.example.com/109.jpg', '描述109', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (110, 'TEST-S-110', '商品10', 100, 100.00, 50.00, 20, 'https://img.example.com/110.jpg', '描述110', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (111, 'TEST-S-111', '商品11', 100, 100.00, 50.00, 20, 'https://img.example.com/111.jpg', '描述111', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (112, 'TEST-S-112', '商品12', 100, 100.00, 50.00, 20, 'https://img.example.com/112.jpg', '描述112', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (113, 'TEST-S-113', '商品13', 100, 100.00, 50.00, 20, 'https://img.example.com/113.jpg', '描述113', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (114, 'TEST-S-114', '商品14', 100, 100.00, 50.00, 20, 'https://img.example.com/114.jpg', '描述114', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (115, 'TEST-S-115', '商品15', 100, 100.00, 50.00, 20, 'https://img.example.com/115.jpg', '描述115', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (116, 'TEST-S-116', '商品16', 100, 100.00, 50.00, 20, 'https://img.example.com/116.jpg', '描述116', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (117, 'TEST-S-117', '商品17', 100, 100.00, 50.00, 20, 'https://img.example.com/117.jpg', '描述117', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (118, 'TEST-S-118', '商品18', 100, 100.00, 50.00, 20, 'https://img.example.com/118.jpg', '描述118', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (119, 'TEST-S-119', '商品19', 100, 100.00, 50.00, 20, 'https://img.example.com/119.jpg', '描述119', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (120, 'TEST-S-120', '商品20', 100, 100.00, 50.00, 20, 'https://img.example.com/120.jpg', '描述120', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (121, 'TEST-S-121', '商品21', 100, 100.00, 50.00, 20, 'https://img.example.com/121.jpg', '描述121', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (122, 'TEST-S-122', '商品22', 100, 100.00, 50.00, 20, 'https://img.example.com/122.jpg', '描述122', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (123, 'TEST-S-123', '商品23', 100, 100.00, 50.00, 20, 'https://img.example.com/123.jpg', '描述123', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (124, 'TEST-S-124', '商品24', 100, 100.00, 50.00, 20, 'https://img.example.com/124.jpg', '描述124', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (125, 'TEST-S-125', '商品25', 100, 100.00, 50.00, 20, 'https://img.example.com/125.jpg', '描述125', 'ON_SHELF', '2026-11-01 10:00:00');
