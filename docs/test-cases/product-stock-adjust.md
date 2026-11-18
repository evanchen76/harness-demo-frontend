# product-stock-adjust 商品調整庫存

後台管理員對單一商品增加或減少庫存，要填調整數量與原因；每次成功調整留下紀錄。上架中的商品扣到 0 自動售罄，售罄商品補貨自動回到上架中。

## API

`POST /api/admin/products/{id}/stock-adjustments`

- header `X-Operator`：操作人（必填）
- request body：
  - `quantity`：整數，正數加、負數扣；必填，不可為 0
  - `reason`：字串，必填，`null`／空字串／只有空白都算沒填；最多 100 字元（中文一字算 1）
- 成功：HTTP 200，`code = OK`，`data` 為調整後的商品（`Product`）
- 失敗：回傳下表的 HTTP 狀態與 `code`；DB 不能有任何變動

| 情況 | HTTP | code |
|------|------|------|
| 缺少 `X-Operator` | 400 | `INVALID_REQUEST` |
| `quantity` 缺少或為 0 | 400 | `STOCK_QUANTITY_INVALID` |
| `reason` 缺少、空白或超過 100 字 | 400 | `STOCK_REASON_INVALID` |
| 商品不存在 | 404 | `PRODUCT_NOT_FOUND` |
| 扣超過現有庫存 | 422 | `PRODUCT_STOCK_INSUFFICIENT` |
| 調整後庫存超過 2147483647 | 422 | `PRODUCT_STOCK_OVERFLOW` |

狀態規則：

| 調整前狀態 | 調整後庫存 | 調整後狀態 |
|------------|------------|------------|
| `ON_SHELF` | 0 | `SOLD_OUT` |
| `ON_SHELF` | > 0 | `ON_SHELF` |
| `SOLD_OUT` | > 0 | `ON_SHELF`（`on_shelf_time` 不變） |
| `DRAFT`、`OFF_SHELF` | 任何 | 不變 |

「DB 不能被改動」指：該商品的 `stock`、`status`、`on_shelf_time` 與 fixture 相同，`product_stock_log` 沒有新資料。

紀錄（`product_stock_log`）一筆包含：`product_id`、`quantity`、`stock_before`、`stock_after`、`status_before`、`status_after`、`reason`、`operator`、`create_time`。

## 測試案例

案例編號在每個 feature 裡從 TC01 開始，標題一律寫成 `### TCxx 標題`，測試方法對應用 `tcxx_` 開頭。

以下沒特別說明時，商品屬於啟用中的分類、庫存 50；請求以 `X-Operator: alice`、`reason = "盤點修正"` 送出。

### TC01 上架中的商品加庫存

- Given：一個 `ON_SHELF` 商品，庫存 50，`on_shelf_time` 為 2026-01-01 10:00:00
- When：`quantity = 10`
- Then：
  - HTTP 200，`code = OK`，`data.stock = 60`，`data.status = ON_SHELF`
  - DB：`product.stock = 60`，`status = ON_SHELF`，`on_shelf_time` 仍為 2026-01-01 10:00:00
  - DB：`product_stock_log` 一筆：`quantity = 10`、`stock_before = 50`、`stock_after = 60`、`status_before = ON_SHELF`、`status_after = ON_SHELF`、`reason = 盤點修正`、`operator = alice`、`create_time` 有值

### TC02 上架中的商品扣庫存

- Given：一個 `ON_SHELF` 商品，庫存 50
- When：`quantity = -20`
- Then：
  - HTTP 200，`code = OK`，`data.stock = 30`，`data.status = ON_SHELF`
  - DB：`product.stock = 30`，`status = ON_SHELF`
  - DB：`product_stock_log` 一筆：`quantity = -20`、`stock_before = 50`、`stock_after = 30`、`status_before = ON_SHELF`、`status_after = ON_SHELF`

### TC03 上架中的商品扣到 0 變售罄

- Given：一個 `ON_SHELF` 商品，庫存 50，`on_shelf_time` 為 2026-01-01 10:00:00
- When：`quantity = -50`
- Then：
  - HTTP 200，`code = OK`，`data.stock = 0`，`data.status = SOLD_OUT`
  - DB：`product.stock = 0`，`status = SOLD_OUT`，`on_shelf_time` 仍為 2026-01-01 10:00:00
  - DB：`product_stock_log` 一筆：`quantity = -50`、`stock_before = 50`、`stock_after = 0`、`status_before = ON_SHELF`、`status_after = SOLD_OUT`

### TC04 售罄商品補貨回到上架中

- Given：一個 `SOLD_OUT` 商品，庫存 0，`on_shelf_time` 為 2026-01-01 10:00:00
- When：`quantity = 5`
- Then：
  - HTTP 200，`code = OK`，`data.stock = 5`，`data.status = ON_SHELF`
  - DB：`product.stock = 5`，`status = ON_SHELF`，`on_shelf_time` 仍為 2026-01-01 10:00:00
  - DB：`product_stock_log` 一筆：`quantity = 5`、`stock_before = 0`、`stock_after = 5`、`status_before = SOLD_OUT`、`status_after = ON_SHELF`

### TC05 草稿商品扣到 0，狀態不變

- Given：一個 `DRAFT` 商品，庫存 50
- When：`quantity = -50`
- Then：
  - HTTP 200，`code = OK`，`data.stock = 0`，`data.status = DRAFT`
  - DB：`product.stock = 0`，`status = DRAFT`
  - DB：`product_stock_log` 一筆：`stock_before = 50`、`stock_after = 0`、`status_before = DRAFT`、`status_after = DRAFT`

### TC06 已下架商品加庫存，狀態不變

- Given：一個 `OFF_SHELF` 商品，庫存 0
- When：`quantity = 30`
- Then：
  - HTTP 200，`code = OK`，`data.stock = 30`，`data.status = OFF_SHELF`
  - DB：`product.stock = 30`，`status = OFF_SHELF`
  - DB：`product_stock_log` 一筆：`stock_before = 0`、`stock_after = 30`、`status_before = OFF_SHELF`、`status_after = OFF_SHELF`

### TC07 原因剛好 100 個中文字

- Given：一個 `ON_SHELF` 商品，庫存 50
- When：`quantity = 1`，`reason` 為 100 個中文字
- Then：
  - HTTP 200，`code = OK`，`data.stock = 51`
  - DB：`product_stock_log` 一筆，`reason` 與送出的 100 字完全相同

### TC08 缺少操作人

- Given：一個 `ON_SHELF` 商品，庫存 50
- When：不帶 `X-Operator`，`quantity = 10`
- Then：HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動

### TC09 調整數量為 0

- Given：一個 `ON_SHELF` 商品，庫存 50
- When：`quantity = 0`
- Then：HTTP 400，`code = STOCK_QUANTITY_INVALID`；DB 不能被改動

### TC10 缺少調整數量

- Given：一個 `ON_SHELF` 商品，庫存 50
- When：body 只有 `reason`，沒有 `quantity`
- Then：HTTP 400，`code = STOCK_QUANTITY_INVALID`；DB 不能被改動

### TC11 缺少原因

- Given：一個 `ON_SHELF` 商品，庫存 50
- When：`quantity = 10`，body 沒有 `reason`
- Then：HTTP 400，`code = STOCK_REASON_INVALID`；DB 不能被改動

### TC12 原因只有空白

- Given：一個 `ON_SHELF` 商品，庫存 50
- When：`quantity = 10`，`reason = "   "`
- Then：HTTP 400，`code = STOCK_REASON_INVALID`；DB 不能被改動

### TC13 原因超過 100 字

- Given：一個 `ON_SHELF` 商品，庫存 50
- When：`quantity = 10`，`reason` 為 101 個中文字
- Then：HTTP 400，`code = STOCK_REASON_INVALID`；DB 不能被改動

### TC14 商品不存在

- Given：DB 沒有 id 999 的商品
- When：對商品 999 送 `quantity = 10`
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`；`product_stock_log` 沒有資料

### TC15 扣超過現有庫存

- Given：一個 `ON_SHELF` 商品，庫存 50
- When：`quantity = -51`
- Then：HTTP 422，`code = PRODUCT_STOCK_INSUFFICIENT`；DB 不能被改動（`stock` 仍為 50，`status` 仍為 `ON_SHELF`，沒有紀錄）

### TC16 加庫存超過上限

- Given：一個 `ON_SHELF` 商品，庫存 2147483600
- When：`quantity = 100`
- Then：HTTP 422，`code = PRODUCT_STOCK_OVERFLOW`；DB 不能被改動

### TC17 兩個人同時扣同一商品，不能超扣

- Given：一個 `ON_SHELF` 商品，庫存 10
- When：兩個請求同時送出（`X-Operator: alice`、`X-Operator: bob`），都是 `quantity = -6`
- Then：
  - 一個 HTTP 200 `code = OK`，另一個 HTTP 422 `code = PRODUCT_STOCK_INSUFFICIENT`
  - DB：`product.stock = 4`，`status = ON_SHELF`
  - DB：`product_stock_log` 只有一筆：`stock_before = 10`、`stock_after = 4`，`operator` 是成功的那個人

### TC18 同時違反多條規則，回傳第一個

- Given：DB 沒有 id 999 的商品
- When：對商品 999 送 `quantity = 0`、`reason = "   "`
- Then：HTTP 400，`code = STOCK_QUANTITY_INVALID`（`quantity` 排在 `reason`、商品存在之前）

## 規則檢查順序

同時違反多條規則時，回傳第一個不通過的：

1. 帶了 `X-Operator`（TC08）
2. `quantity` 有值且不為 0（TC09、TC10、TC18）
3. `reason` 有值、不是空白、不超過 100 字（TC07、TC11～TC13）
4. 商品存在（TC14）
5. 調整後庫存 `>= 0`（TC15、TC17）
6. 調整後庫存 `<= 2147483647`（TC16）
