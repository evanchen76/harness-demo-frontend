-- storefront-product-browse TC07：每頁筆數用到上限 50
-- Given 55 個 ON_SHELF 商品
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
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (126, 'TEST-S-126', '商品26', 100, 100.00, 50.00, 20, 'https://img.example.com/126.jpg', '描述126', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (127, 'TEST-S-127', '商品27', 100, 100.00, 50.00, 20, 'https://img.example.com/127.jpg', '描述127', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (128, 'TEST-S-128', '商品28', 100, 100.00, 50.00, 20, 'https://img.example.com/128.jpg', '描述128', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (129, 'TEST-S-129', '商品29', 100, 100.00, 50.00, 20, 'https://img.example.com/129.jpg', '描述129', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (130, 'TEST-S-130', '商品30', 100, 100.00, 50.00, 20, 'https://img.example.com/130.jpg', '描述130', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (131, 'TEST-S-131', '商品31', 100, 100.00, 50.00, 20, 'https://img.example.com/131.jpg', '描述131', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (132, 'TEST-S-132', '商品32', 100, 100.00, 50.00, 20, 'https://img.example.com/132.jpg', '描述132', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (133, 'TEST-S-133', '商品33', 100, 100.00, 50.00, 20, 'https://img.example.com/133.jpg', '描述133', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (134, 'TEST-S-134', '商品34', 100, 100.00, 50.00, 20, 'https://img.example.com/134.jpg', '描述134', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (135, 'TEST-S-135', '商品35', 100, 100.00, 50.00, 20, 'https://img.example.com/135.jpg', '描述135', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (136, 'TEST-S-136', '商品36', 100, 100.00, 50.00, 20, 'https://img.example.com/136.jpg', '描述136', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (137, 'TEST-S-137', '商品37', 100, 100.00, 50.00, 20, 'https://img.example.com/137.jpg', '描述137', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (138, 'TEST-S-138', '商品38', 100, 100.00, 50.00, 20, 'https://img.example.com/138.jpg', '描述138', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (139, 'TEST-S-139', '商品39', 100, 100.00, 50.00, 20, 'https://img.example.com/139.jpg', '描述139', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (140, 'TEST-S-140', '商品40', 100, 100.00, 50.00, 20, 'https://img.example.com/140.jpg', '描述140', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (141, 'TEST-S-141', '商品41', 100, 100.00, 50.00, 20, 'https://img.example.com/141.jpg', '描述141', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (142, 'TEST-S-142', '商品42', 100, 100.00, 50.00, 20, 'https://img.example.com/142.jpg', '描述142', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (143, 'TEST-S-143', '商品43', 100, 100.00, 50.00, 20, 'https://img.example.com/143.jpg', '描述143', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (144, 'TEST-S-144', '商品44', 100, 100.00, 50.00, 20, 'https://img.example.com/144.jpg', '描述144', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (145, 'TEST-S-145', '商品45', 100, 100.00, 50.00, 20, 'https://img.example.com/145.jpg', '描述145', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (146, 'TEST-S-146', '商品46', 100, 100.00, 50.00, 20, 'https://img.example.com/146.jpg', '描述146', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (147, 'TEST-S-147', '商品47', 100, 100.00, 50.00, 20, 'https://img.example.com/147.jpg', '描述147', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (148, 'TEST-S-148', '商品48', 100, 100.00, 50.00, 20, 'https://img.example.com/148.jpg', '描述148', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (149, 'TEST-S-149', '商品49', 100, 100.00, 50.00, 20, 'https://img.example.com/149.jpg', '描述149', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (150, 'TEST-S-150', '商品50', 100, 100.00, 50.00, 20, 'https://img.example.com/150.jpg', '描述150', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (151, 'TEST-S-151', '商品51', 100, 100.00, 50.00, 20, 'https://img.example.com/151.jpg', '描述151', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (152, 'TEST-S-152', '商品52', 100, 100.00, 50.00, 20, 'https://img.example.com/152.jpg', '描述152', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (153, 'TEST-S-153', '商品53', 100, 100.00, 50.00, 20, 'https://img.example.com/153.jpg', '描述153', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (154, 'TEST-S-154', '商品54', 100, 100.00, 50.00, 20, 'https://img.example.com/154.jpg', '描述154', 'ON_SHELF', '2026-11-01 10:00:00');
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (155, 'TEST-S-155', '商品55', 100, 100.00, 50.00, 20, 'https://img.example.com/155.jpg', '描述155', 'ON_SHELF', '2026-11-01 10:00:00');
