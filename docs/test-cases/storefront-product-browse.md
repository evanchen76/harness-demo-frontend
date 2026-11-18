# storefront-product-browse 前台商品展示

消費者不用登入就能瀏覽商品：列表（分類篩選、名稱搜尋、排序、分頁）與詳情。只看得到「上架中且分類啟用」的商品；不顯示 sku、成本價、庫存數量（庫存剩 1～9 時只提示「只剩 N」）。畫面不做，API 只回英文 key 與代碼，中文文案由前端處理。

## API

### `GET /api/storefront/products` 列表

- header：不需要 `X-Operator`
- query（都選填）：
  - `page`：正整數，從 1 開始，預設 1
  - `size`：正整數，預設 20，上限 50
  - `sort`：`latest`（預設）、`price_asc`、`price_desc`
  - `categoryId`：正整數，只看該分類
  - `keyword`：名稱包含、不分大小寫；空白視為沒帶
- 成功：HTTP 200，`code = OK`，`data` 為 `{ items, page, size, total }`；`total` 是符合條件的總數（不受分頁影響）
- 每筆只含：`id`、`name`、`price`、`mainImageUrl`、`categoryName`、`stockStatus`、`remaining`
- `stockStatus`：庫存 1～9 為 `LOW_STOCK`，`remaining` 為剩餘數；庫存 10 以上為 `IN_STOCK`，`remaining` 為 `null`
- 可見條件：`status = ON_SHELF` 且分類 `enabled = 1`
- 排序：`latest` 依 `onShelfTime` 新到舊；`price_asc` 價格低到高；`price_desc` 價格高到低；同值一律依 `id` 新到舊
- 失敗：HTTP 400，`code = INVALID_REQUEST`（`page`、`size`、`categoryId` 不是正整數，`size` 超過 50，`sort` 不認得）
- 唯讀：DB 不會有任何變動

### `GET /api/storefront/products/{id}` 詳情

- header：不需要 `X-Operator`
- 成功：HTTP 200，`code = OK`，`data` 為列表欄位加 `description`
- 看不到的商品（不存在、id 不是數字、草稿、已下架、已售完、分類停用）：一律 HTTP 404，`code = PRODUCT_NOT_FOUND`，`message = 找不到商品`，回應內容完全相同
- 唯讀：DB 不會有任何變動

## 測試案例

案例編號在每個 feature 裡從 TC01 開始，標題一律寫成 `### TCxx 標題`，測試方法對應用 `tcxx_` 開頭。以下案例都不帶 `X-Operator`，且 DB 不能被改動（成功與失敗都一樣）。

### TC01 列表只含上架中且分類啟用的商品

- Given：啟用中的分類 A；停用的分類 B；分類 A 下五個商品，狀態分別為 `ON_SHELF`（兩個）、`DRAFT`、`OFF_SHELF`、`SOLD_OUT`；分類 B 下一個 `ON_SHELF` 商品
- When：呼叫 `GET /api/storefront/products`
- Then：
  - HTTP 200，`code = OK`
  - `data.items` 只有分類 A 下那兩個 `ON_SHELF` 商品，`data.total` 為 2
  - DB：不變

### TC02 列表每筆只含指定欄位

- Given：啟用中的分類 A（name 為「家電」）；一個 `ON_SHELF` 商品，`sku`、`cost_price`、`stock`、`description`、`name`、`price`、`main_image_url` 都有值
- When：呼叫 `GET /api/storefront/products`
- Then：
  - HTTP 200，`code = OK`，`data.items` 共 1 筆
  - 該筆有 `id`、`name`、`price`、`mainImageUrl`、`categoryName`（值為「家電」）、`stockStatus`、`remaining`，值與 DB 相同
  - 該筆沒有 `sku`、`costPrice`、`stock`、`description`、`status`、`categoryId`、`onShelfTime`、`createTime`、`updateTime` 這些 key
  - DB：不變

### TC03 庫存提示的邊界

- Given：啟用中的分類；四個 `ON_SHELF` 商品，庫存分別為 1、9、10、500
- When：呼叫 `GET /api/storefront/products`
- Then：
  - HTTP 200，`code = OK`，`data.items` 共 4 筆
  - 庫存 1：`stockStatus = LOW_STOCK`，`remaining = 1`
  - 庫存 9：`stockStatus = LOW_STOCK`，`remaining = 9`
  - 庫存 10：`stockStatus = IN_STOCK`，`remaining = null`
  - 庫存 500：`stockStatus = IN_STOCK`，`remaining = null`
  - 任何一筆都沒有 `stock` key
  - DB：不變

### TC04 預設分頁

- Given：啟用中的分類；25 個 `ON_SHELF` 商品
- When：呼叫 `GET /api/storefront/products`（不帶 `page`、`size`）
- Then：
  - HTTP 200，`code = OK`
  - `data.page = 1`，`data.size = 20`，`data.total = 25`，`data.items` 共 20 筆
  - DB：不變

### TC05 指定頁碼與每頁筆數

- Given：同 TC04（25 個 `ON_SHELF` 商品，預設排序下依 id 新到舊可預期）
- When：呼叫 `GET /api/storefront/products?page=3&size=10`
- Then：
  - HTTP 200，`code = OK`
  - `data.page = 3`，`data.size = 10`，`data.total = 25`，`data.items` 共 5 筆，為排序後第 21～25 筆
  - DB：不變

### TC06 頁碼超出最後一頁回空列表

- Given：同 TC04（25 個 `ON_SHELF` 商品）
- When：呼叫 `GET /api/storefront/products?page=9&size=20`
- Then：
  - HTTP 200，`code = OK`
  - `data.items` 為 `[]`，`data.page = 9`，`data.total = 25`
  - DB：不變

### TC07 每頁筆數可用到上限 50

- Given：啟用中的分類；55 個 `ON_SHELF` 商品
- When：呼叫 `GET /api/storefront/products?size=50`
- Then：
  - HTTP 200，`code = OK`
  - `data.size = 50`，`data.items` 共 50 筆，`data.total = 55`
  - DB：不變

### TC08 每頁筆數超過上限

- Given：啟用中的分類；一個 `ON_SHELF` 商品（其他條件都符合，只有 `size` 違規）
- When：呼叫 `GET /api/storefront/products?size=51`
- Then：HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動

### TC09 page 不是正整數

- Given：同 TC08
- When：分別呼叫 `?page=0`、`?page=-1`、`?page=abc`
- Then：三次都是 HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動

### TC10 size 不是正整數

- Given：同 TC08
- When：分別呼叫 `?size=0`、`?size=-5`、`?size=abc`
- Then：三次都是 HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動

### TC11 預設依最新上架排序，同時間依 id 新到舊

- Given：啟用中的分類；四個 `ON_SHELF` 商品：id 10（`on_shelf_time` 2026-11-01 10:00）、id 20（2026-11-03 10:00）、id 30（2026-11-02 10:00）、id 40（2026-11-03 10:00，與 id 20 相同）
- When：呼叫 `GET /api/storefront/products`；再呼叫一次 `?sort=latest`
- Then：
  - 兩次都是 HTTP 200，`code = OK`
  - `data.items` 的 id 順序都是 40、20、30、10
  - DB：不變

### TC12 價格由低到高

- Given：啟用中的分類；四個 `ON_SHELF` 商品：id 10（價格 300）、id 20（100）、id 30（200）、id 40（100，與 id 20 同價）
- When：呼叫 `GET /api/storefront/products?sort=price_asc`
- Then：HTTP 200，`code = OK`，`data.items` 的 id 順序為 40、20、30、10；DB：不變

### TC13 價格由高到低

- Given：同 TC12
- When：呼叫 `GET /api/storefront/products?sort=price_desc`
- Then：HTTP 200，`code = OK`，`data.items` 的 id 順序為 10、30、40、20（同價 40 在 20 前）；DB：不變

### TC14 sort 不認得

- Given：啟用中的分類；一個 `ON_SHELF` 商品
- When：呼叫 `GET /api/storefront/products?sort=random`
- Then：HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動

### TC15 依分類篩選

- Given：兩個啟用中的分類 A、B；分類 A 下兩個 `ON_SHELF` 商品，分類 B 下三個 `ON_SHELF` 商品
- When：呼叫 `GET /api/storefront/products?categoryId=<A 的 id>`
- Then：HTTP 200，`code = OK`，`data.items` 只有分類 A 的兩個商品，`categoryName` 都是 A 的名稱，`data.total = 2`；DB：不變

### TC16 篩選的分類已停用回空列表

- Given：停用的分類 B，下有一個 `ON_SHELF` 商品
- When：呼叫 `GET /api/storefront/products?categoryId=<B 的 id>`
- Then：HTTP 200，`code = OK`，`data.items` 為 `[]`，`data.total = 0`；DB：不變

### TC17 篩選的分類不存在回空列表

- Given：啟用中的分類下有 `ON_SHELF` 商品；沒有 id 為 99999 的分類
- When：呼叫 `GET /api/storefront/products?categoryId=99999`
- Then：HTTP 200，`code = OK`，`data.items` 為 `[]`，`data.total = 0`；DB：不變

### TC18 categoryId 不是正整數

- Given：啟用中的分類下有一個 `ON_SHELF` 商品
- When：分別呼叫 `?categoryId=0`、`?categoryId=abc`
- Then：兩次都是 HTTP 400，`code = INVALID_REQUEST`；DB 不能被改動

### TC19 名稱搜尋：包含、不分大小寫

- Given：啟用中的分類；四個 `ON_SHELF` 商品，名稱分別為「Apple 手機」、「蘋果 apple 充電線」、「香蕉」、「APPLE」
- When：呼叫 `GET /api/storefront/products?keyword=apple`
- Then：HTTP 200，`code = OK`，`data.items` 為名稱含 apple（不分大小寫）的三個商品，不含「香蕉」，`data.total = 3`；DB：不變

### TC20 空白關鍵字視為沒帶

- Given：啟用中的分類；三個 `ON_SHELF` 商品
- When：分別呼叫 `?keyword=`（空字串）、`?keyword=%20%20`（只有空白）
- Then：兩次都是 HTTP 200，`code = OK`，`data.items` 共 3 筆（與沒帶 `keyword` 相同）；DB：不變

### TC21 名稱搜尋不會搜到不可見的商品

- Given：啟用中的分類 A；停用的分類 B；名稱都含「手機」的商品：分類 A 的 `ON_SHELF`、分類 A 的 `OFF_SHELF`、分類 B 的 `ON_SHELF`
- When：呼叫 `GET /api/storefront/products?keyword=手機`
- Then：HTTP 200，`code = OK`，`data.items` 只有分類 A 的 `ON_SHELF` 那一個，`data.total = 1`；DB：不變

### TC22 篩選、搜尋、排序、分頁可以組合

- Given：啟用中的分類 A、B；分類 A 下五個 `ON_SHELF` 商品名稱含「杯」，價格 50、40、30、20、10；分類 A 下一個名稱不含「杯」；分類 B 下一個名稱含「杯」
- When：呼叫 `GET /api/storefront/products?categoryId=<A 的 id>&keyword=杯&sort=price_asc&page=2&size=2`
- Then：HTTP 200，`code = OK`，`data.total = 5`，`data.items` 為價格 30、40 兩筆（價格由低到高的第 3、4 筆）；DB：不變

### TC23 沒有任何可見商品時回空列表

- Given：DB 有分類，但商品都不可見（只有一個 `DRAFT` 商品）
- When：呼叫 `GET /api/storefront/products`
- Then：HTTP 200，`code = OK`，`data.items` 為 `[]`，`data.page = 1`，`data.size = 20`，`data.total = 0`；DB：不變

### TC24 詳情：列表欄位加商品描述

- Given：啟用中的分類 A（name 為「家電」）；一個 `ON_SHELF` 商品，庫存 50，`sku`、`cost_price`、`description`、`main_image_url` 都有值
- When：呼叫 `GET /api/storefront/products/<該商品 id>`
- Then：
  - HTTP 200，`code = OK`
  - `data` 有 `id`、`name`、`price`、`mainImageUrl`、`categoryName`（「家電」）、`stockStatus = IN_STOCK`、`remaining = null`、`description`，值與 DB 相同
  - `data` 沒有 `sku`、`costPrice`、`stock`、`status`、`categoryId`、`onShelfTime`、`createTime`、`updateTime` 這些 key
  - DB：不變

### TC25 詳情：庫存剩 1～9 顯示只剩多少

- Given：啟用中的分類；兩個 `ON_SHELF` 商品，庫存分別為 9、10
- When：分別呼叫兩個商品的詳情
- Then：庫存 9 的商品 `stockStatus = LOW_STOCK`、`remaining = 9`；庫存 10 的商品 `stockStatus = IN_STOCK`、`remaining = null`；兩者都是 HTTP 200，`code = OK`，都沒有 `stock` key；DB：不變

### TC26 詳情：草稿商品找不到

- Given：啟用中的分類；一個 `DRAFT` 商品
- When：呼叫該商品的詳情
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`，`message = 找不到商品`；DB 不能被改動

### TC27 詳情：已下架商品找不到

- Given：啟用中的分類；一個 `OFF_SHELF` 商品
- When：呼叫該商品的詳情
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`，`message = 找不到商品`；DB 不能被改動

### TC28 詳情：已售完商品找不到

- Given：啟用中的分類；一個 `SOLD_OUT` 商品
- When：呼叫該商品的詳情
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`，`message = 找不到商品`；DB 不能被改動

### TC29 詳情：分類停用的上架商品找不到

- Given：停用的分類；分類下一個 `ON_SHELF` 商品
- When：呼叫該商品的詳情
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`，`message = 找不到商品`；DB 不能被改動

### TC30 詳情：商品不存在

- Given：沒有 id 為 99999 的商品
- When：呼叫 `GET /api/storefront/products/99999`
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`，`message = 找不到商品`；DB 不能被改動

### TC31 詳情：id 不是數字也回找不到

- Given：任意資料（啟用中的分類與一個 `ON_SHELF` 商品）
- When：呼叫 `GET /api/storefront/products/abc`
- Then：HTTP 404，`code = PRODUCT_NOT_FOUND`，`message = 找不到商品`（不是 400）；DB 不能被改動

### TC32 看不到的商品回應內容完全相同，不透露原因

- Given：啟用中的分類一個，停用的分類一個；商品各一個：`DRAFT`、`OFF_SHELF`、`SOLD_OUT`、停用分類下的 `ON_SHELF`；沒有 id 99999 的商品
- When：分別呼叫上述四個商品與 99999 的詳情
- Then：五次回應的 HTTP 狀態、`code`、`message`、`data`（為 `null`）完全一樣；DB 不能被改動

## 規則檢查順序

列表的參數檢查在查詢之前，同時有多個參數不合法時只要回 400 `INVALID_REQUEST`，不區分是哪一個（不測順序）。詳情只有一種失敗（找不到），沒有檢查順序。
