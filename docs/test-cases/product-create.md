# product-create 新增商品

後台營運人員新增一筆商品。新增後固定是草稿（`DRAFT`）、庫存 0；上架、調整庫存走各自的 API。

## API

`POST /api/admin/products`

- header `X-Operator`：操作人（必填；這次不寫入 DB）
- request body（JSON）：

  | 欄位 | 必填 | 限制 |
  |------|------|------|
  | `sku` | 是 | 不可空白，最長 64 字，不可與既有商品重複 |
  | `categoryId` | 是 | 分類必須存在且啟用 |
  | `price` | 是 | 不可為負，最多兩位小數（0 可以，上架時才檢查是否合理） |
  | `name` | 否 | 最長 200 字 |
  | `costPrice` | 否 | 不可為負，最多兩位小數；沒傳為 0 |
  | `mainImageUrl` | 否 | 最長 500 字 |
  | `description` | 否 | 無長度限制（TEXT） |

- 成功：HTTP 201，`code = OK`，`data` 為新增的商品（`Product`）
- 失敗：回傳下表的 HTTP 狀態與 `code`；DB 不能有任何變動

| 情況 | HTTP | code |
|------|------|------|
| 缺少 `X-Operator` | 400 | `INVALID_REQUEST` |
| 缺必填、格式、長度、數值不合 | 400 | `PRODUCT_FIELD_INVALID` |
| `sku` 已存在 | 409 | `PRODUCT_SKU_DUPLICATE` |
| 分類不存在 | 422 | `CATEGORY_NOT_FOUND` |
| 分類停用（`enabled = 0`） | 422 | `CATEGORY_DISABLED` |

「DB 不能被改動」指：`product` 表的筆數與每筆內容都跟 fixture 相同。

## 測試案例

案例編號在每個 feature 裡從 TC01 開始，標題一律寫成 `### TCxx 標題`，測試方法對應用 `tcxx_` 開頭。

以下「其他欄位合法」指：`sku`、`categoryId`、`price` 三個必填欄位都有且符合限制，選填欄位不傳。每個失敗案例只違反一條規則（TC20～TC22 除外）。

### TC01 只填必填欄位新增成功

- Given：啟用中的分類（id 100）；DB 沒有 `SKU-NEW`
- When：以 `X-Operator: alice` 新增商品，body 只有 `sku = SKU-NEW`、`categoryId = 100`、`price = 1290`
- Then：
  - HTTP 201，`code = OK`，`data.sku = SKU-NEW`，`data.status = DRAFT`，`data.stock = 0`，`data.costPrice = 0`，`data.name`、`data.mainImageUrl`、`data.description`、`data.onShelfTime` 為 null，`data.id` 有值
  - DB：`product` 多一筆，欄位與回應一致，`name`、`main_image_url`、`description`、`on_shelf_time` 為 null

### TC02 填全部欄位新增成功

- Given：啟用中的分類（id 100）；DB 沒有 `SKU-FULL`
- When：以 `X-Operator: alice` 新增商品，body：`sku = SKU-FULL`、`name = 無線滑鼠`、`categoryId = 100`、`price = 590`、`costPrice = 300.5`、`mainImageUrl = https://img.example.com/mouse.jpg`、`description = 靜音、2.4G`
- Then：
  - HTTP 201，`code = OK`，`data` 的每個欄位照傳入的值，`data.status = DRAFT`，`data.stock = 0`
  - DB：`product` 多一筆，欄位照存，`status = DRAFT`，`stock = 0`

### TC03 售價與成本為 0 可以新增

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，`sku = SKU-ZERO`、`categoryId = 100`、`price = 0`、`costPrice = 0`
- Then：
  - HTTP 201，`code = OK`，`data.price = 0`，`data.costPrice = 0`（草稿不檢查價格是否合理，上架時才檢查）
  - DB：`product` 多一筆，`price = 0`

### TC04 各欄位剛好在上限可以新增

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品：`sku` 64 個字、`name` 200 個字、`mainImageUrl` 500 個字、`price = 99999999.99`、`costPrice = 0.01`、`categoryId = 100`
- Then：
  - HTTP 201，`code = OK`，各欄位完整保存、不被截斷
  - DB：`product` 多一筆，`sku`、`name`、`main_image_url` 長度分別為 64、200、500，`price = 99999999.99`

### TC05 缺少操作人

- Given：啟用中的分類（id 100）
- When：不帶 `X-Operator` 新增商品，body 為合法的 `sku = SKU-NEW`、`categoryId = 100`、`price = 1290`
- Then：HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動（`product` 表筆數不變）

### TC06 缺少 sku

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，body 沒有 `sku`，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC07 缺少 categoryId

- Given：DB 有商品分類
- When：以 `X-Operator: alice` 新增商品，body 沒有 `categoryId`，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC08 缺少 price

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，body 沒有 `price`，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC09 sku 只有空白

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，`sku = '   '`，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC10 sku 超過 64 個字

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，`sku` 65 個字，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC11 name 超過 200 個字

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，`name` 201 個字，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC12 mainImageUrl 超過 500 個字

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，`mainImageUrl` 501 個字，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC13 price 為負

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，`price = -1`，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC14 price 超過兩位小數

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，`price = 10.123`，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC15 costPrice 為負

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，`costPrice = -0.01`，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC16 costPrice 超過兩位小數

- Given：啟用中的分類（id 100）
- When：以 `X-Operator: alice` 新增商品，`costPrice = 5.555`，其他欄位合法
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）

### TC17 sku 已存在

- Given：啟用中的分類（id 100）；DB 已有一個 `sku = SKU-DUP` 的商品
- When：以 `X-Operator: alice` 新增商品，`sku = SKU-DUP`，其他欄位合法
- Then：HTTP 409，`code = PRODUCT_SKU_DUPLICATE`；DB 不能被改動（`product` 表筆數不變，既有那筆的欄位不變）

### TC18 分類不存在

- Given：DB 沒有 id 999 的分類
- When：以 `X-Operator: alice` 新增商品，`categoryId = 999`，其他欄位合法
- Then：HTTP 422，`code = CATEGORY_NOT_FOUND`；DB 不能被改動（`product` 表筆數不變）

### TC19 分類已停用

- Given：停用中的分類（`enabled = 0`，id 101）
- When：以 `X-Operator: alice` 新增商品，`categoryId = 101`，其他欄位合法
- Then：HTTP 422，`code = CATEGORY_DISABLED`；DB 不能被改動（`product` 表筆數不變）

### TC20 缺少操作人又缺少 sku，回傳操作人的錯

- Given：啟用中的分類（id 100）
- When：不帶 `X-Operator`，body 也沒有 `sku`
- Then：HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動（`product` 表筆數不變）（操作人排在請求內容之前）

### TC21 請求內容不合又分類不存在，回傳請求內容的錯

- Given：DB 沒有 id 999 的分類
- When：以 `X-Operator: alice` 新增商品，`categoryId = 999`、`price = -1`
- Then：HTTP 400，`code = PRODUCT_FIELD_INVALID`；DB 不能被改動（`product` 表筆數不變）（請求內容排在分類之前）

### TC22 分類已停用又 sku 重複，回傳分類的錯

- Given：停用中的分類（id 101）；DB 已有 `sku = SKU-DUP` 的商品
- When：以 `X-Operator: alice` 新增商品，`sku = SKU-DUP`、`categoryId = 101`
- Then：HTTP 422，`code = CATEGORY_DISABLED`；DB 不能被改動（`product` 表筆數不變）（分類排在 `sku` 重複之前）

## 規則檢查順序

同時違反多條規則時，回傳第一個不通過的：

1. 帶了 `X-Operator`（TC05、TC20）
2. 請求內容：必填、格式、長度、數值（TC06～TC16、TC21）
3. 分類存在（TC18）
4. 分類啟用（TC19、TC22）
5. `sku` 不重複（TC17）；兩個請求同時用同一個 `sku` 時，DB 唯一鍵也要擋下並回同一個錯誤（見 requirement.md 決策 5）

## 完成標準對照

`requirement.md` 的完成標準每一條對應一個代表案例；一條涵蓋多個案例時，全部列在這裡，這些案例都要過：

| 完成標準 | 案例 |
|------|------|
| D1 只填必填欄位成功 | TC01 |
| D2 填全部欄位成功 | TC02（邊界：TC03、TC04） |
| D3 缺 `X-Operator` | TC05（順序：TC20） |
| D4 缺 `sku`、`categoryId` 或 `price` | TC06、TC07、TC08 |
| D5 欄位格式不合 | TC09～TC16（順序：TC21） |
| D6 `sku` 已存在 | TC17 |
| D7 分類不存在或停用 | TC18、TC19（順序：TC22） |
