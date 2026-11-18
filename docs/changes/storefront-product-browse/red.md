# storefront-product-browse 紅燈記錄

實作前跑 `./dev.sh check` 的結果。失敗訊息照 `target/surefire-reports/` 原文貼上，沒有改寫。

- 時間：2026-11-16（綠燈階段動手前；`src/main` 還沒有任何前台商品相關程式）
- 基準 commit：`bf25856`，加上未 commit 的 `StorefrontProductBrowseApiTest.java`、`fixtures/storefront-product-browse/`、`openapi.yaml` 與測案文件（規格階段、測案階段的產出）
- 結果：`CHECK: FAIL（紅燈）`

## 總數

| 範圍 | 執行 | 失敗（Failures） | 錯誤（Errors） | 略過 |
|------|------|------------------|----------------|------|
| `StorefrontProductBrowseApiTest` | 32 | 0 | 32 | 0 |
| 全部測試 | 130 | 0 | 32 | 0 |

其他 7 個測試類別（98 個測試，含 `ArchitectureTest`）全綠，紅的只有這次新增的 32 個。

## 逐案對照

測案來源：`docs/test-cases/storefront-product-browse.md`（TC01～TC32，共 32 案，全部有對應測試方法）。

| 測案 | 測試方法 | 結果 | 失敗訊息 |
|------|----------|------|----------|
| TC01 列表只含上架中且分類啟用的商品 | `tc01_visibleOnly:36` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC02 列表每筆只含指定欄位 | `tc02_fields:52` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC03 庫存提示的邊界 | `tc03_stockHint:82` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC04 預設分頁 | `tc04_defaultPaging:110` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC05 指定頁碼與每頁筆數 | `tc05_pageAndSize:127` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC06 頁碼超出最後一頁回空列表 | `tc06_pageBeyondLast:147` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC07 每頁筆數可用到上限 50 | `tc07_sizeMax:163` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC08 每頁筆數超過上限 | `tc08_sizeOverMax:179` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC09 page 不是正整數 | `tc09_pageInvalid:192` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC10 size 不是正整數 | `tc10_sizeInvalid:207` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC11 預設依最新上架排序，同時間依 id 新到舊 | `tc11_sortLatest:222` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC12 價格由低到高 | `tc12_priceAsc:243` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC13 價格由高到低 | `tc13_priceDesc:261` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC14 sort 不認得 | `tc14_sortUnknown:279` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC15 依分類篩選 | `tc15_filterByCategory:292` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC16 篩選的分類已停用回空列表 | `tc16_categoryDisabled:309` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC17 篩選的分類不存在回空列表 | `tc17_categoryMissing:324` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC18 categoryId 不是正整數 | `tc18_categoryIdInvalid:339` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC19 名稱搜尋：包含、不分大小寫 | `tc19_keyword:354` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC20 空白關鍵字視為沒帶 | `tc20_keywordBlank:370` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC21 名稱搜尋不會搜到不可見的商品 | `tc21_keywordVisibleOnly:386` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC22 篩選、搜尋、排序、分頁可以組合 | `tc22_combined:402` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC23 沒有任何可見商品時回空列表 | `tc23_noVisibleProducts:425` | 錯誤 | `Response status 404 not defined for path '/api/storefront/products'.` |
| TC24 詳情：列表欄位加商品描述 | `tc24_detail:444` | 錯誤 | `GET on path '/api/storefront/products/{id}' defines a response schema but no response body found.` |
| TC25 詳情：庫存剩 1～9 顯示只剩多少 | `tc25_detailStockHint:473` | 錯誤 | `GET on path '/api/storefront/products/{id}' defines a response schema but no response body found.` |
| TC26 詳情：草稿商品找不到 | `tc26_detailDraft:495` | 錯誤 | `GET on path '/api/storefront/products/{id}' defines a response schema but no response body found.` |
| TC27 詳情：已下架商品找不到 | `tc27_detailOffShelf:502` | 錯誤 | `GET on path '/api/storefront/products/{id}' defines a response schema but no response body found.` |
| TC28 詳情：已售完商品找不到 | `tc28_detailSoldOut:509` | 錯誤 | `GET on path '/api/storefront/products/{id}' defines a response schema but no response body found.` |
| TC29 詳情：分類停用的上架商品找不到 | `tc29_detailCategoryDisabled:516` | 錯誤 | `GET on path '/api/storefront/products/{id}' defines a response schema but no response body found.` |
| TC30 詳情：商品不存在 | `tc30_detailNotExist:523` | 錯誤 | `GET on path '/api/storefront/products/{id}' defines a response schema but no response body found.` |
| TC31 詳情：id 不是數字也回找不到 | `tc31_detailNotNumber:530` | 錯誤 | `GET on path '/api/storefront/products/{id}' defines a response schema but no response body found.` |
| TC32 看不到的商品回應內容完全相同，不透露原因 | `tc32_detailSameResponse:537` | 錯誤 | `GET on path '/api/storefront/products/{id}' defines a response schema but no response body found.` |

## 紅的原因

- 32 案都是**錯誤（Errors）不是失敗（Failures）**：例外是 `OpenApiValidation`，在 `mockMvc.perform(...)` 那一行就丟出，後面的 `andExpect(...)` 斷言一條都沒執行到。
- 列表 23 案（TC01～TC23）是「Response status 404 not defined」：`GET /api/storefront/products` 還沒有實作（Spring 回 404 空 body），而 `openapi.yaml` 的列表只定義 200、400，驗證器先擋下來。
- 詳情 9 案（TC24～TC32，其中 TC24、TC25 預期 200，其餘預期 404）是「defines a response schema but no response body found」：路徑還沒實作，回的 404 沒有 body。
- 全部紅的原因都是「功能還沒做」。沒有編譯錯、fixture 載入失敗，也沒有 `validation.request.*`（請求驗證）的訊息，代表 query 參數（含 `size=51`、`page=abc`、`sort=random`）與詳情的 `id=abc` 都能通過規格驗證，紅燈階段開頭擔心的「請求被驗證先擋下」沒有發生。
- 所以這次紅燈能證明的是「端點還沒做、fixture 載得進去、測試跑得起來」；**不能證明**各案的斷言本身有效，那要等綠燈階段實作過程中逐案轉綠才看得出來。

## 需要注意

- TC26～TC32 的 404 預期 body 是 `PRODUCT_NOT_FOUND`＋「找不到商品」，`openapi.yaml` 的 404 回應用 `ApiResponse`，實作後應能通過回應驗證；如果實作後仍紅在這裡，是規格問題，要回報。
