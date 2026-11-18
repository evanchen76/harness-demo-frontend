# product-update 編輯商品

後台營運人員修改一筆既有商品的資料。只改有傳的欄位；`sku`、庫存、狀態不能透過編輯修改（庫存走調整庫存，狀態走上架）。

## API

`PATCH /api/admin/products/{id}`

- header `X-Operator`：操作人（必填；這次不寫入 DB）
- request body（JSON）：至少要有一個可編輯欄位；沒傳的欄位不改

  | 欄位 | 限制 |
  |------|------|
  | `name` | 最長 200 字；`null`、空字串、只有空白表示清空（存成 null） |
  | `categoryId` | 不可為 null；分類必須存在且啟用 |
  | `price` | 不可為 null、不可為負、最多兩位小數 |
  | `costPrice` | 不可為負、最多兩位小數 |
  | `mainImageUrl` | 最長 500 字；清空規則同 `name` |
  | `description` | 清空規則同 `name` |

  帶 `sku`、`stock`、`status` 或任何不認得的欄位，整個請求拒絕。
- 成功：HTTP 200，`code = OK`，`data` 為編輯後的商品（`Product`）
- 可編輯的狀態：`DRAFT`、`ON_SHELF`、`OFF_SHELF`、`SOLD_OUT` 都可以
- `ON_SHELF` 的商品編輯後必須仍符合上架條件：`name`、`mainImageUrl`、`description` 都有值、`price > 0` 且 `price >= costPrice`、分類存在且啟用（不檢查庫存，因為編輯改不到庫存）
- 失敗：回傳下表的 HTTP 狀態與 `code`；DB 不能有任何變動

| 情況 | HTTP | code |
|------|------|------|
| 缺少 `X-Operator` | 400 | `INVALID_REQUEST` |
| 商品不存在 | 404 | `PRODUCT_NOT_FOUND` |
| 不可編輯欄位、沒有任何欄位、格式、長度、數值不合、`categoryId` 或 `price` 傳 null | 400 | `PRODUCT_FIELD_INVALID` |
| `ON_SHELF` 商品編輯後 `name`、`mainImageUrl`、`description` 任一為空 | 422 | `PRODUCT_INFO_INCOMPLETE` |
| `ON_SHELF` 商品編輯後 `price <= 0` 或 `price < costPrice` | 422 | `PRODUCT_PRICE_INVALID` |
| 換成的分類不存在 | 422 | `CATEGORY_NOT_FOUND` |
| 換成的分類停用（`enabled = 0`） | 422 | `CATEGORY_DISABLED` |

「DB 不能被改動」指：該商品的每個欄位（含 `update_time`）都跟 fixture 相同。

## 測試案例

案例編號在每個 feature 裡從 TC01 開始，標題一律寫成 `### TCxx 標題`，測試方法對應用 `tcxx_` 開頭。

以下「基準商品」指：`sku = SKU-BASE`；`name = 舊名稱`；分類 id 100（啟用中）；售價 1290、成本 800；庫存 50；`main_image_url`、`description` 都有值；狀態依案例而定。每個失敗案例只違反一條規則（TC28～TC30 除外）。

### TC01 DRAFT 商品只改名稱

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `name = 新名稱`
- Then：
  - HTTP 200，`code = OK`，`data.name = 新名稱`，其他欄位（`sku`、`categoryId`、`price`、`costPrice`、`stock`、`mainImageUrl`、`description`、`status`）與原本相同
  - DB：只有 `name` 變成新名稱；`stock`、`status`、`sku` 與其他欄位不變

### TC02 DRAFT 商品一次改全部可編輯欄位

- Given：一個 `DRAFT` 基準商品（id 100）；另有一個啟用中的分類（id 102）
- When：以 `X-Operator: alice` 編輯商品 100，body：`name`、`categoryId = 102`、`price`、`costPrice`、`mainImageUrl`、`description` 全部有傳新值
- Then：
  - HTTP 200，`code = OK`，`data` 的六個欄位都是新值，`sku`、`stock`、`status` 不變
  - DB：六個欄位都是新值；`sku`、`stock`、`status` 不變

### TC03 DRAFT 商品把選填欄位清空

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body：`name = null`、`mainImageUrl = ''`、`description = '   '`
- Then：
  - HTTP 200，`code = OK`，`data.name`、`data.mainImageUrl`、`data.description` 都是 null
  - DB：這三個欄位都是 null（空字串、只有空白也存成 null）；其他欄位不變

### TC04 OFF_SHELF 商品可以編輯

- Given：一個 `OFF_SHELF` 基準商品（id 100），庫存 50
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `price = 1500`
- Then：
  - HTTP 200，`code = OK`，`data.price = 1500`，`data.status = OFF_SHELF`，`data.stock = 50`
  - DB：`price = 1500`；`status`、`stock`、`on_shelf_time` 不變

### TC05 SOLD_OUT 商品可以編輯

- Given：一個 `SOLD_OUT` 基準商品（id 100），庫存 0
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `description = 補貨中`
- Then：
  - HTTP 200，`code = OK`，`data.description = 補貨中`，`data.status = SOLD_OUT`，`data.stock = 0`
  - DB：`description` 改變；`status`、`stock` 不變

### TC06 ON_SHELF 商品改名稱與描述成功

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00
- When：以 `X-Operator: alice` 編輯商品 100，body：`name = 新名稱`、`description = 新描述`
- Then：
  - HTTP 200，`code = OK`，`data.name`、`data.description` 為新值，`data.status = ON_SHELF`
  - DB：`name`、`description` 為新值；`status`、`stock`、`on_shelf_time` 不變

### TC07 ON_SHELF 商品改價格，仍符合上架條件

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00；價格 1290、成本 800
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `price = 900`
- Then：
  - HTTP 200，`code = OK`，`data.price = 900`，`data.status = ON_SHELF`
  - DB：`price = 900`；`status`、`on_shelf_time` 不變

### TC08 ON_SHELF 商品價格改成 0

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `price = 0`（成本 0 時也一樣：價格要大於 0）
- Then：HTTP 422，`code = PRODUCT_PRICE_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC09 ON_SHELF 商品價格改成低於成本

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00；成本 800
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `price = 799`
- Then：HTTP 422，`code = PRODUCT_PRICE_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC10 ON_SHELF 商品把名稱清空

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `name = null`
- Then：HTTP 422，`code = PRODUCT_INFO_INCOMPLETE`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC11 ON_SHELF 商品主圖網址改成空白

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `mainImageUrl = '   '`
- Then：HTTP 422，`code = PRODUCT_INFO_INCOMPLETE`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC12 ON_SHELF 商品描述改成空字串

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `description = ''`
- Then：HTTP 422，`code = PRODUCT_INFO_INCOMPLETE`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC13 ON_SHELF 商品換成不存在的分類

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `categoryId = 999`
- Then：HTTP 422，`code = CATEGORY_NOT_FOUND`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC14 ON_SHELF 商品換成停用的分類

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00；停用中的分類（`enabled = 0`，id 101）
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `categoryId = 101`
- Then：HTTP 422，`code = CATEGORY_DISABLED`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC15 DRAFT 商品換成不存在的分類

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `categoryId = 999`
- Then：HTTP 422，`code = CATEGORY_NOT_FOUND`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC16 DRAFT 商品換成停用的分類

- Given：一個 `DRAFT` 基準商品（id 100）；停用中的分類（id 101）
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `categoryId = 101`
- Then：HTTP 422，`code = CATEGORY_DISABLED`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC17 body 帶 sku

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body：`sku = SKU-OTHER`、`name = 新名稱`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）（連同合法的 `name` 也不改）

### TC18 body 帶 stock

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body：`stock = 999`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC19 body 帶 status

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body：`status = ON_SHELF`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC20 body 沒有任何欄位

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body 為 `{}`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC21 name 超過 200 個字

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `name` 201 個字
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC22 price 為負

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `price = -1`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC23 price 超過兩位小數

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `price = 10.123`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC24 categoryId 傳 null

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `categoryId = null`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC25 price 傳 null

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body 只有 `price = null`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC26 缺少操作人

- Given：一個 `DRAFT` 基準商品（id 100）
- When：不帶 `X-Operator` 編輯商品 100，body 只有 `name = 新名稱`
- Then：HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動（該商品所有欄位與 fixture 相同）

### TC27 商品不存在

- Given：DB 沒有 id 999 的商品
- When：以 `X-Operator: alice` 編輯商品 999，body 只有 `name = 新名稱`
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`

### TC28 商品不存在又帶不可編輯欄位，回傳商品不存在

- Given：DB 沒有 id 999 的商品
- When：以 `X-Operator: alice` 編輯商品 999，body：`sku = SKU-OTHER`
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`（商品存在排在請求內容之前）

### TC29 請求內容不合又分類不存在，回傳請求內容的錯

- Given：一個 `DRAFT` 基準商品（id 100）
- When：以 `X-Operator: alice` 編輯商品 100，body：`categoryId = 999`、`price = -1`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（該商品所有欄位與 fixture 相同）（請求內容排在分類之前）

### TC30 ON_SHELF 商品換不存在的分類又清空名稱，回傳分類的錯

- Given：一個 `ON_SHELF` 基準商品（id 100），`on_shelf_time` 為 2026-01-01 10:00:00
- When：以 `X-Operator: alice` 編輯商品 100，body：`categoryId = 999`、`name = null`
- Then：HTTP 422，`code = CATEGORY_NOT_FOUND`；DB 不能被改動（該商品所有欄位與 fixture 相同）（分類排在上架條件之前）

## 規則檢查順序

同時違反多條規則時，回傳第一個不通過的：

1. 帶了 `X-Operator`（TC26）
2. 商品存在（TC27、TC28）
3. 請求內容：不可編輯欄位、至少一個欄位、格式、長度、數值、null 限制（TC17～TC25、TC29）
4. 換分類時，分類存在、再來啟用（TC13～TC16、TC30）；沒傳 `categoryId` 就不檢查
5. 只有 `ON_SHELF`：編輯後資料完整，再來價格（TC08～TC12）

## 完成標準對照

`requirement.md` 的完成標準每一條對應一個代表案例；一條涵蓋多個案例時，全部列在這裡，這些案例都要過：

| 完成標準 | 案例 |
|------|------|
| D8 `DRAFT` 商品只改有傳的欄位 | TC01（多欄位：TC02；清空：TC03） |
| D9 `OFF_SHELF`、`SOLD_OUT` 可以編輯 | TC04、TC05 |
| D10 `ON_SHELF` 改不影響上架條件的欄位 | TC06（改價格：TC07） |
| D11 `ON_SHELF` 改後不符合上架條件 | TC08～TC14（順序：TC30） |
| D12 body 帶不可編輯欄位 | TC17、TC18、TC19 |
| D13 body 沒有任何可編輯欄位 | TC20 |
| D14 格式不合、缺操作人、商品不存在、分類不存在或停用 | TC21～TC27、TC15、TC16（順序：TC28、TC29） |
