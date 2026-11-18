-- storefront-product-browse TC17：篩選的分類不存在
-- Given 啟用的分類 100 下有 ON_SHELF 商品；沒有分類 99999
INSERT INTO category (id, name, enabled) VALUES (100, '測試分類', 1);
INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time)
VALUES (101, 'TEST-S-101', '商品一', 100, 100.00, 50.00, 20, 'https://img.example.com/101.jpg', '描述101', 'ON_SHELF', '2026-11-01 10:00:00');
