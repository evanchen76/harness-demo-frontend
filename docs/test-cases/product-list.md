# product-list 商品列表

後台列出全部商品，每筆顯示列表欄位（不含商品描述、成本價）；不分頁，依 id 由小到大。中文欄位標題與狀態顯示由前端處理，API 只回英文 key 與 `status` 代碼。

## API

`GET /api/admin/products`

- header：不需要 `X-Operator`
- request：沒有 body；不接受篩選或分頁參數（帶了也忽略）
- 成功：HTTP 200，`code = OK`，`data` 為商品陣列，依 `id` 由小到大；沒有商品時為 `[]`
- 每筆只含：`id`、`sku`、`name`、`categoryId`、`price`、`stock`、`status`、`mainImageUrl`、`onShelfTime`、`updateTime`；不含 `description`、`costPrice`、`createTime`
- 唯讀：DB 不會有任何變動
- 沒有失敗案例（沒有必填欄位、不需權限、也沒有「找不到」）

## 測試案例

案例編號在每個 feature 裡從 TC01 開始，標題一律寫成 `### TCxx 標題`，測試方法對應用 `tcxx_` 開頭。

### TC01 依 id 由小到大列出全部商品

- Given：啟用中的分類；三個商品，以 id 300、100、200 的順序寫入 DB，`sku`、`name`、`price`、`stock`、`main_image_url` 各不相同，狀態皆為 `ON_SHELF`，`on_shelf_time` 皆有值
- When：不帶 `X-Operator`，呼叫 `GET /api/admin/products`
- Then：
  - HTTP 200，`code = OK`
  - `data` 共 3 筆，順序為 id 100、200、300
  - 每筆的 `id`、`sku`、`name`、`categoryId`、`price`、`stock`、`status`、`mainImageUrl`、`onShelfTime`、`updateTime` 與 DB 相同
  - DB：不變

### TC02 列表不含描述、成本價、建立時間

- Given：啟用中的分類；一個商品，`description` 有值、`cost_price` 為 800
- When：呼叫 `GET /api/admin/products`
- Then：
  - HTTP 200，`code = OK`，`data` 共 1 筆
  - 該筆沒有 `description`、`costPrice`、`createTime` 這三個 key
  - DB：不變

### TC03 四種狀態都以英文代碼列出

- Given：啟用中的分類；四個商品，狀態分別為 `DRAFT`、`ON_SHELF`、`OFF_SHELF`、`SOLD_OUT`
- When：呼叫 `GET /api/admin/products`
- Then：
  - HTTP 200，`code = OK`，`data` 共 4 筆
  - 四筆的 `status` 分別是 `DRAFT`、`ON_SHELF`、`OFF_SHELF`、`SOLD_OUT`，各出現一次
  - DB：不變

### TC04 沒有商品時回空陣列

- Given：DB 裡沒有任何商品（可以有分類）
- When：呼叫 `GET /api/admin/products`
- Then：HTTP 200，`code = OK`，`data` 為 `[]`；DB：不變

### TC05 沒填的欄位回 null

- Given：啟用中的分類；一個 `DRAFT` 商品，`name`、`main_image_url` 為 null，`on_shelf_time` 為 null（從未上架）
- When：呼叫 `GET /api/admin/products`
- Then：
  - HTTP 200，`code = OK`，`data` 共 1 筆
  - 該筆的 `name`、`mainImageUrl`、`onShelfTime` 為 null，其餘列表欄位正常有值
  - DB：不變

## 規則檢查順序

這個 API 只有一條成功路徑，沒有需要排序的檢查。
