# 前台商品展示

## 原始需求

新需求：前台商品展示

【目的】
目前商品只能在後台管理，消費者看不到商品。這次要開放消費者瀏覽商品，先做 API，畫面另外做。

【功能】

1. 商品列表
   * 可以依分類篩選、用商品名稱搜尋
   * 可以排序：最新上架（預設）、價格低到高、價格高到低
   * 要分頁，每頁筆數有上限
   * 顯示：商品名稱、售價、主圖、分類名稱
2. 商品詳情
   * 顯示列表的資訊，加上商品描述

【規則】

* 不用登入就能瀏覽
* 只顯示上架中的商品；分類被停用的話，底下的商品也不顯示
* 看不到的商品開詳情時，一律回「找不到商品」，不透露是下架還是其他原因
* 不能顯示成本價、庫存數量、商品編號

【待確認】

* 售完的商品要不要顯示？如果要，要標示「已售完」
* 要不要顯示有沒有貨

【不做】

* 購物車、下單、會員
* 後台功能不改

## 釐清問答

AI 在釐清階段提出的問題與使用者的回答。規則以這裡的回答為準，不自己假設。

| # | 問題 | 回答 |
|---|------|------|
| Q1 | 售完（SOLD_OUT）的商品要不要顯示？（「只顯示上架中」與「售完要標示已售完」互相衝突，要擇一） | 只顯示上架中 |
| Q2 | 要不要顯示有沒有貨？（若 Q1 顯示售完商品，列表與詳情要多回一個欄位，例如 soldOut 布林值；不回庫存數量） | 少於 10 時顯示「只剩 N」，其他顯示「有庫存」（使用者回答；這是「不能顯示庫存數量」的明確例外，只在剩 1～9 時回數字） |
| Q3 | 路徑要用什麼？建議 GET /api/products 與 GET /api/products/{id}（前台，不需 X-Operator） | 要跟後台拆開一點（使用者回答）→ AI 採用 /api/storefront/products 與 /api/storefront/products/{id}，見決策 |
| Q4 | 分頁參數與上限？建議 page（從 1 開始，預設 1）、size（預設 20、上限 50）；超過上限是自動壓成 50，還是回 400？page、size 不是正整數時回 400 還是用預設值？ | 照 AI 建議：page 從 1 開始預設 1；size 預設 20、上限 50；size 超過 50、page 或 size 不是正整數都回 400 INVALID_REQUEST |
| Q5 | 分頁回傳格式？建議 data 內放 items、page、size、total | 照 AI 建議：data 內放 items、page、size、total |
| Q6 | 排序參數？建議 sort=latest（預設）、price_asc、price_desc；不認得的值回 400 還是用預設？「最新上架」依 onShelfTime 新到舊；同值時依 id 新到舊，OK 嗎？ | 照 AI 建議：sort=latest（預設）、price_asc、price_desc；不認得的值回 400 INVALID_REQUEST；latest 依 onShelfTime 新到舊，同值依 id 新到舊；價格排序同價依 id 新到舊 |
| Q7 | 名稱搜尋怎麼比對？建議 keyword 參數、名稱「包含」關鍵字、不分大小寫；空白關鍵字視為沒帶；商品名稱可為空（DB 允許 NULL），沒名稱的商品也要顯示嗎？ | 照 AI 建議：keyword 參數，名稱包含、不分大小寫；空白視為沒帶。上架條件已保證 name 不為空，沒名稱的情況不會出現 |
| Q8 | 分類篩選？建議 categoryId 參數；指定的分類不存在或已停用時，回 200 空列表還是 4xx？ | 照 AI 建議：categoryId 參數；分類不存在或已停用都回 200 空列表（total 0）；categoryId 不是正整數回 400 INVALID_REQUEST |
| Q9 | 主圖沒設定時回 null 可以嗎？ | 照 AI 建議：上架條件已保證主圖不為空；仍照 DB 值回傳，不特別處理 |
| Q10 | 回傳欄位名稱：id（商品代號，用在詳情路徑）、name、price、mainImageUrl、categoryName、description（只有詳情）。id 是流水號，不算「商品編號」（sku），OK 嗎？ | 照 AI 建議：id（流水號，用在詳情路徑）、name、price、mainImageUrl、categoryName、stockStatus、remaining、description（只有詳情）；id 不算商品編號 sku |
| Q11 | 詳情的商品 id 不存在、非數字、下架、草稿、分類停用，是否都回 404 + 同一個錯誤訊息「找不到商品」（非數字也回 404，不回 400）？ | 照 AI 建議：商品不存在、非數字、下架、草稿、售完、分類停用，一律 404 PRODUCT_NOT_FOUND，訊息「找不到商品」 |
| Q12 | 要不要順便做「分類列表」API 讓畫面做篩選選單？（目前需求沒寫，預設不做） | 不用 |

## 完成標準

這個需求做到什麼才算完成。釐清階段就寫，規格核准時跟規格一起給使用者確認。只寫「結果」（端點、預設值、回傳欄位、錯誤情境）；型別、分層這類過程檢查由 `ArchitectureTest` 擔保，不用重寫。

每一條都要能對應到檢查：行為對應測案編號（測案階段寫好後回填），慣例對應 `ArchitectureTest` 規則；對應不到的，改寫成可驗證的說法，或標「人工確認」並說明為什麼。

| # | 完成標準 | 對應檢查 |
|---|----------|----------|
| D1 | GET /api/storefront/products 不需登入也不需 X-Operator，回 200；只含 ON_SHELF 且分類啟用的商品，DRAFT、OFF_SHELF、SOLD_OUT、分類停用的都不出現 | TC01、TC23、TC21 |
| D2 | 列表每筆只含 id、name、price、mainImageUrl、categoryName、stockStatus、remaining；不含 sku、costPrice、stock、description 與其他欄位 | TC02、TC24 |
| D3 | stock 1～9：stockStatus 為 LOW_STOCK、remaining 為剩餘數；stock ≥ 10：stockStatus 為 IN_STOCK、remaining 為 null（邊界 9 與 10 都要測） | TC03、TC25 |
| D4 | 分頁：page 預設 1、size 預設 20、上限 50；回 data.items、page、size、total；超出最後一頁回空 items、total 不變；size 超過 50、page 或 size 非正整數回 400 INVALID_REQUEST | TC04、TC05、TC06、TC07、TC08、TC09、TC10 |
| D5 | 排序 sort：latest（預設，onShelfTime 新到舊）、price_asc、price_desc；同值依 id 新到舊；不認得的值回 400 INVALID_REQUEST | TC11、TC12、TC13、TC14 |
| D6 | categoryId 篩選：只回該分類的商品；分類不存在或停用回 200 空列表；categoryId 非正整數回 400 INVALID_REQUEST | TC15、TC16、TC17、TC18 |
| D7 | keyword 搜尋：名稱包含、不分大小寫；空白視為沒帶；可與 categoryId、sort、分頁組合 | TC19、TC20、TC21、TC22 |
| D8 | GET /api/storefront/products/{id}：可見商品回 200，欄位為列表欄位加 description | TC24 |
| D9 | 詳情看不到的商品（不存在、非數字、DRAFT、OFF_SHELF、SOLD_OUT、分類停用）一律回 404 PRODUCT_NOT_FOUND「找不到商品」，回應內容完全相同，不透露原因 | TC26、TC27、TC28、TC29、TC30、TC31、TC32 |
| D10 | 兩支 API 都是唯讀，不改動 DB；後台 /api/admin 的行為不變 | 每個測案的「DB 不變」（TC01～TC32）；後台行為不變：既有後台測試維持全綠（check） |

## 範圍

這次要改的東西。規格核准時跟規格一起給使用者確認。

- DB：無（讀 product、category 既有欄位）
- API：新增前台商品列表與詳情：GET /api/storefront/products、GET /api/storefront/products/{id}
- 測案：`docs/test-cases/storefront-product-browse.md`

## 不做的事

明確排除的項目。做的時候看到範圍外想改的，寫在回報裡（review 頁的 `findings`），不動手。

- 購物車、下單、會員
- 不做頁面（只做 API）
- 後台功能不改
- 不做分類列表 API（Q12）
- 不做登入、限流、快取

## 決策

測案或規格沒寫到、做的時候自己決定的事。之後的 session 照著做，不要推翻。

| 日期 | 決策 | 理由 |
|------|------|------|
| 2026-11-16 | 前台路徑用 /api/storefront/products（列表）與 /api/storefront/products/{id}（詳情） | 使用者要求跟後台（/api/admin）拆開，沒指定名稱，AI 選 storefront |
| 2026-11-16 | 庫存提示用兩個欄位 stockStatus（IN_STOCK / LOW_STOCK）與 remaining（LOW_STOCK 才有數字，否則 null）；門檻：stock < 10 為 LOW_STOCK | 使用者回答「少於 10 顯示只剩多少，其他顯示有庫存」；上架條件保證 stock > 0，所以不會有 0 |
| 2026-11-16 | 只顯示 ON_SHELF；SOLD_OUT 視同看不到，不顯示「已售完」 | 使用者回答 Q1 |
| 2026-11-16 | 參數錯誤一律 400 INVALID_REQUEST（不自動修正） | AI 判斷：與 OpenAPI 驗證一致，行為單純；紅燈階段若發現請求驗證在進到 controller 前就擋下，要回報再議 |
| 2026-11-16 | 前台 404 的 code 沿用 PRODUCT_NOT_FOUND，message 在前台回「找不到商品」；後台的 message 維持「商品不存在」 | 需求指定前台訊息，且「後台功能不改」；不新增 ErrorCode，實作時前台用自訂 message |
| 2026-11-16 | openapi.yaml 的前台 query 參數與詳情 path id 都宣告成 string、不寫範圍與 enum | 不合格的值要由服務回 400（詳情非數字回 404），規格寫範圍會被測試的請求驗證先擋下（同既有 CreateRequest 的做法）；範圍寫在 description |
| 2026-11-16 | page、size、categoryId 有帶但是空字串，也算不合格（400）；只有 keyword 的空白視為沒帶 | 測案只定義 keyword 空白；其他參數照「不是正整數回 400」字面處理 |
| 2026-11-16 | keyword 轉小寫比對，並把 `\`、`%`、`_` 當成一般字元（跳脫 LIKE 特殊字元）；頁碼大到會溢位時視為超過最後一頁，回空 items | 避免使用者輸入 `%` 變成萬用字元；測案沒涵蓋，這是實作上的保護 |
| 2026-11-16 | 前台 id 在 Service 用正規式判斷（正整數且在 long 範圍內才查 DB），其餘直接當找不到 | TC31：非數字 id 回 404 而不是 400 |
