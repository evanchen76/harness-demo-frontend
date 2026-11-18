# 商品列表

## 原始需求

列表每筆顯示：編號（id）、商品編號（sku）、商品名稱（name）、分類（categoryId）、售價（price）、庫存（stock）、狀態（status）、主圖（mainImageUrl）、最近上架時間（onShelfTime）、更新時間（updateTime）。不含商品描述和成本價。

顯示的內容盡量用中文，包含欄位標題；狀態也顯示成中文（草稿、上架中、已下架、已售完）。

不分頁，排序依編號（id）。

## 釐清問答

AI 在釐清階段提出的問題與使用者的回答。規則以這裡的回答為準，不自己假設。

| # | 問題 | 回答 |
|---|------|------|
| Q1 | 「欄位標題、狀態用中文」怎麼實現？專案只有 JSON API，沒有前端頁面 | 這個專案只做後端、不做頁面；中文標題與狀態由前端處理，API 不用管。回英文 key 與 status 代碼 |
| Q2 | 價格、時間的顯示格式？ | 沿用現有格式（與 GET 單筆商品相同），未上架過的 onShelfTime 為 null |
| Q3 | 沒有任何商品時回什麼？ | 200 + 空陣列 |
| Q4 | 要不要篩選條件？要不要 X-Operator？ | 都不要：只有無參數列表；不要求 X-Operator（同 GET 單筆） |

## 完成標準

這個需求做到什麼才算完成。釐清階段就寫，規格核准時跟規格一起給使用者確認。只寫「結果」（端點、預設值、回傳欄位、錯誤情境）；型別、分層這類過程檢查由 `ArchitectureTest` 擔保，不用重寫。

每一條都要能對應到檢查：行為對應測案編號（測案階段寫好後回填），慣例對應 `ArchitectureTest` 規則；對應不到的，改寫成可驗證的說法，或標「人工確認」並說明為什麼。

| # | 完成標準 | 對應檢查 |
|---|----------|----------|
| D1 | GET /api/admin/products 回 200，data 為全部商品的陣列，不分頁，依 id 由小到大 | TC01 |
| D2 | 每筆只含 id、sku、name、categoryId、price、stock、status、mainImageUrl、onShelfTime、updateTime；不含 description、costPrice（也不含 createTime） | TC01、TC02、TC05 |
| D3 | status 回英文代碼（DRAFT、ON_SHELF、OFF_SHELF、SOLD_OUT），四種狀態都能正確列出 | TC03 |
| D4 | 沒有任何商品時回 200，data 為 [] | TC04 |
| D5 | 不需要 X-Operator，不接受篩選或分頁參數；列表為唯讀，不改動 DB | TC01（不帶 X-Operator、DB 不變）；「不接受篩選或分頁參數」人工確認：openapi.yaml 的 GET 沒有宣告任何參數，測試的 OpenAPI 驗證不允許帶規格外的參數，寫不成測案 |

## 範圍

這次要改的東西。規格核准時跟規格一起給使用者確認。

- DB：無
- API：新增 GET /api/admin/products（既有路徑已有 POST）
- 測案：`docs/test-cases/product-list.md`

## 不做的事

明確排除的項目。做的時候看到範圍外想改的，寫在回報裡（review 頁的 `findings`），不動手。

- 不做頁面、不做中文標題與中文狀態（前端處理）
- 不分頁、不篩選、不排序選項

## 決策

測案或規格沒寫到、做的時候自己決定的事。之後的 session 照著做，不要推翻。

| 日期 | 決策 | 理由 |
|------|------|------|
| 2026-11-16 | 刪掉 TC06（帶篩選或分頁參數也忽略），D5 的參數部分改人工確認 | 紅燈階段發現：TC06 帶的 status、page、size 不在 openapi.yaml，被請求驗證擋下，端點做完仍會紅；規格是「不接受」這些參數，不宣告、不測（使用者決定） |
