# product-on-shelf 商品上架

後台營運人員把草稿（或已下架）商品上架，上架後前台才看得到。上架前要確認商品資料完整、價格與庫存合理、分類可用。

## API

`POST /api/admin/products/{id}/on-shelf`

- header `X-Operator`：操作人（必填）
- request body：不需要 request body
- 成功：HTTP 200，`code = OK`，`data` 為上架後的商品（`Product`）
- 失敗：回傳下表的 HTTP 狀態與 `code`；DB 不能有任何變動

| 情況 | HTTP | code |
|------|------|------|
| 缺少 `X-Operator` | 400 | `INVALID_REQUEST` |
| 商品不存在 | 404 | `PRODUCT_NOT_FOUND` |
| 商品已上架 | 409 | `PRODUCT_ALREADY_ON_SHELF` |
| `name`、`main_image_url`、`description` 任一為 null／空白 | 422 | `PRODUCT_INFO_INCOMPLETE` |
| `price <= 0` 或 `price < cost_price` | 422 | `PRODUCT_PRICE_INVALID` |
| `stock <= 0` | 422 | `PRODUCT_STOCK_INVALID` |
| 分類不存在 | 422 | `CATEGORY_NOT_FOUND` |
| 分類停用（`enabled = 0`） | 422 | `CATEGORY_DISABLED` |

「DB 不能被改動」指：該商品的 `status`、`on_shelf_time` 與 fixture 相同。

## 測試案例

案例編號在每個 feature 裡從 TC01 開始，標題一律寫成 `### TCxx 標題`，測試方法對應用 `tcxx_` 開頭。

以下「可上架的商品」指：分類存在且啟用；`name`、`main_image_url`、`description` 都有值；售價 1290、成本 800、庫存 50。每個失敗案例只違反一條規則（TC15 除外）。

### TC01 草稿商品上架成功

- Given：啟用中的分類；一個可上架的 `DRAFT` 商品，`on_shelf_time` 為 null
- When：以 `X-Operator: alice` 上架該商品
- Then：
  - HTTP 200，`code = OK`，`data.id` 為該商品，`data.status = ON_SHELF`，`data.onShelfTime` 有值
  - DB：`product.status = ON_SHELF`，`product.on_shelf_time` 不為 null

### TC02 已下架商品重新上架成功

- Given：啟用中的分類；一個可上架的 `OFF_SHELF` 商品，`on_shelf_time` 為 2026-01-01 10:00:00
- When：以 `X-Operator: alice` 上架該商品
- Then：
  - HTTP 200，`code = OK`，`data.status = ON_SHELF`
  - DB：`product.status = ON_SHELF`，`product.on_shelf_time` 晚於 2026-01-01 10:00:00

### TC03 售價等於成本可以上架

- Given：啟用中的分類；一個可上架的 `DRAFT` 商品，但售價 800、成本 800
- When：以 `X-Operator: alice` 上架該商品
- Then：
  - HTTP 200，`code = OK`，`data.status = ON_SHELF`
  - DB：`product.status = ON_SHELF`

### TC04 缺少操作人

- Given：啟用中的分類；一個可上架的 `DRAFT` 商品
- When：不帶 `X-Operator` 上架該商品
- Then：HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動

### TC05 商品不存在

- Given：DB 沒有 id 999 的商品
- When：以 `X-Operator: alice` 上架商品 999
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`

### TC06 商品已經上架

- Given：啟用中的分類；一個可上架的 `ON_SHELF` 商品，`on_shelf_time` 為 2026-01-01 10:00:00
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 409，`code = PRODUCT_ALREADY_ON_SHELF`；DB 不能被改動（`on_shelf_time` 仍為 2026-01-01 10:00:00）

### TC07 缺少商品名稱

- Given：啟用中的分類；一個 `DRAFT` 商品，`name` 為 null，其他條件都符合
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 422，`code = PRODUCT_INFO_INCOMPLETE`；DB 不能被改動

### TC08 主圖網址只有空白

- Given：啟用中的分類；一個 `DRAFT` 商品，`main_image_url` 為 `'   '`，其他條件都符合
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 422，`code = PRODUCT_INFO_INCOMPLETE`；DB 不能被改動

### TC09 缺少商品描述

- Given：啟用中的分類；一個 `DRAFT` 商品，`description` 為 null，其他條件都符合
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 422，`code = PRODUCT_INFO_INCOMPLETE`；DB 不能被改動

### TC10 售價為 0

- Given：啟用中的分類；一個 `DRAFT` 商品，售價 0、成本 0，其他條件都符合
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 422，`code = PRODUCT_PRICE_INVALID`；DB 不能被改動

### TC11 售價低於成本

- Given：啟用中的分類；一個 `DRAFT` 商品，售價 799、成本 800，其他條件都符合
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 422，`code = PRODUCT_PRICE_INVALID`；DB 不能被改動

### TC12 庫存為 0

- Given：啟用中的分類；一個 `DRAFT` 商品，庫存 0，其他條件都符合
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 422，`code = PRODUCT_STOCK_INVALID`；DB 不能被改動

### TC13 分類不存在

- Given：一個 `DRAFT` 商品，`category_id` 指向不存在的分類，其他條件都符合
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 422，`code = CATEGORY_NOT_FOUND`；DB 不能被改動

### TC14 分類已停用

- Given：停用中的分類（`enabled = 0`）；一個 `DRAFT` 商品屬於該分類，其他條件都符合
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 422，`code = CATEGORY_DISABLED`；DB 不能被改動

### TC15 同時違反多條規則，回傳第一個

- Given：停用中的分類；一個 `DRAFT` 商品屬於該分類，`name` 為 null、庫存 0
- When：以 `X-Operator: alice` 上架該商品
- Then：HTTP 422，`code = PRODUCT_INFO_INCOMPLETE`（資料完整排在庫存、分類之前）；DB 不能被改動

## 規則檢查順序

同時違反多條規則時，回傳第一個不通過的：

1. 帶了 `X-Operator`（TC04）
2. 商品存在（TC05）
3. 狀態是 `DRAFT` 或 `OFF_SHELF`（TC06）
4. 資料完整：`name`、`main_image_url`、`description`（TC07～TC09、TC15）
5. 價格：`price > 0` 且 `price >= cost_price`（TC03、TC10、TC11）
6. 庫存：`stock > 0`（TC12）
7. 分類存在（TC13）
8. 分類啟用（TC14）
