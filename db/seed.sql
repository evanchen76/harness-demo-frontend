-- shop 重建後載入的預設資料，給起服務、打 API 用（測試開始前會清空，各自用 fixture）。
-- id 用 1～99；fixture 用 100 以上，兩者不會撞。

INSERT INTO category (id, name, enabled) VALUES
  (1, '3C 配件', 1),
  (2, '季節限定', 0);

INSERT INTO product (id, sku, name, category_id, price, cost_price, stock, main_image_url, description, status, on_shelf_time) VALUES
  (1, 'SKU-0001', '無線藍牙耳機',   1, 1290.00, 800.00, 50, 'https://img.example.com/sku-0001.jpg', '降噪、續航 30 小時', 'DRAFT',    NULL),
  (2, 'SKU-0002', 'USB-C 快充線 1m', 1,  290.00, 120.00, 200, 'https://img.example.com/sku-0002.jpg', '60W PD',             'ON_SHELF', NOW() - INTERVAL 3 DAY),
  (3, 'SKU-0003', '聖誕限定保溫杯', 2,  690.00, 350.00, 30, 'https://img.example.com/sku-0003.jpg', NULL,                 'DRAFT',    NULL);
