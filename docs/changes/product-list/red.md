# product-list 紅燈記錄

實作前跑 `./dev.sh check` 的結果。失敗訊息照 `target/surefire-reports/` 原文貼上，沒有改寫。

- 時間：2026-11-15（綠燈階段動手前；`src/main` 還沒有任何列表相關程式）
- 基準 commit：`10350ca`，加上未 commit 的 `ProductListApiTest.java` 與 `fixtures/product-list/`
- 結果：`CHECK: FAIL（紅燈）`

## 總數

| 範圍 | 執行 | 失敗（Failures） | 錯誤（Errors） | 略過 |
|------|------|------------------|----------------|------|
| `ProductListApiTest` | 6 | 0 | 6 | 0 |
| 全部測試 | 99 | 0 | 6 | 0 |

其他 6 個測試類別（93 個測試）全綠，紅的只有這次新增的 6 個。

## 逐案對照

測案來源：`docs/test-cases/product-list.md`（TC01～TC06，共 6 案，全部有對應測試方法）。

| 測案 | 測試方法 | 結果 | 失敗訊息 |
|------|----------|------|----------|
| TC01 依 id 由小到大列出全部商品 | `tc01_orderedById:32` | 錯誤 | `Response status 405 not defined for path '/api/admin/products'.` |
| TC02 列表不含描述、成本價、建立時間 | `tc02_excludedFields:63` | 錯誤 | `Response status 405 not defined for path '/api/admin/products'.` |
| TC03 四種狀態都以英文代碼列出 | `tc03_allStatuses:81` | 錯誤 | `Response status 405 not defined for path '/api/admin/products'.` |
| TC04 沒有商品時回空陣列 | `tc04_noProducts:95` | 錯誤 | `Response status 405 not defined for path '/api/admin/products'.` |
| TC05 沒填的欄位回 null | `tc05_nullFields:109` | 錯誤 | `Response status 405 not defined for path '/api/admin/products'.` |
| TC06 帶篩選或分頁參數也忽略 | `tc06_paramsIgnored:133` | 錯誤 | 同上的 405，另外多三條：`Query parameter 'page' is unexpected on path "/api/admin/products"`、`Query parameter 'size' is unexpected …`、`Query parameter 'status' is unexpected …` |

## 紅的原因

- 六案都是**錯誤（Errors）不是失敗（Failures）**：例外是 `OpenApiValidation`，在 `mockMvc.perform(...)` 那一行就丟出，後面的 `andExpect(...)` 斷言一條都沒執行到。
- 405 的意思：`GET /api/admin/products` 還沒有實作（同路徑只有 `POST`），而 `openapi.yaml` 這個 path 的 GET 只定義了 200，所以驗證器先擋下來。
- 所以這次紅燈能證明的是「端點還沒做、fixture 載得進去、測試跑得起來」；**不能證明**各案的斷言本身有效，那要等綠燈階段實作過程中逐案轉綠才看得出來。

## 需要注意：TC06

TC06 除了 405，還有三條 `validation.request.parameter.query.unexpected`。這三條是**請求**驗證，跟端點有沒有實作無關：`openapi.yaml` 的 GET 沒有宣告 `status`、`page`、`size`，驗證器不接受規格外的 query 參數。照現在的寫法，綠燈階段把端點做出來之後 TC06 仍然會紅。

這是測案（帶了也忽略）跟規格驗證（不准帶）之間的衝突。

**處理（2026-11-16，使用者決定）：刪掉 TC06**。測案、測試方法、fixture 都已移除，`requirement.md` 的 D5 參數部分改人工確認，並記進決策。上面的表格保留當時六案的原始結果；刪除後重跑的結果見下一節。

## 刪掉 TC06 後重跑（2026-11-16）

`./dev.sh check`：`CHECK: FAIL（紅燈）`

| 範圍 | 執行 | 失敗（Failures） | 錯誤（Errors） | 略過 |
|------|------|------------------|----------------|------|
| `ProductListApiTest` | 5 | 0 | 5 | 0 |
| 全部測試 | 98 | 0 | 5 | 0 |

TC01～TC05 五案都還是同一條 `Response status 405 not defined for path '/api/admin/products'.`，沒有其他種類的錯誤；剩下的紅燈都是「端點還沒做」。
