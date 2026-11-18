# <feature 名稱>

<這個 feature 對應的原始需求，一兩句>

## API

`<METHOD> <path>`

- header `X-Operator`：操作人（必填）
- request body：<欄位；不需要就寫「不需要 request body」>
- 成功：HTTP 200，`code = OK`，`data` 為 <回傳什麼>
- 失敗：回傳下表的 HTTP 狀態與 `code`；DB 不能有任何變動

## 測試案例

案例編號在每個 feature 裡從 TC01 開始，標題一律寫成 `### TCxx 標題`，測試方法對應用 `tcxx_` 開頭。

### TC01 <成功案例的標題>

- Given：<這個案例需要的資料，例如：啟用中的分類；一個草稿商品，售價 1290、庫存 50>
- When：<呼叫什麼，例如：以 `X-Operator: alice` 上架該商品>
- Then：
  - HTTP 200，`code = OK`，<回應裡要檢查的欄位>
  - DB：<哪些表、哪些欄位要變成什麼>

### TC02 <失敗案例的標題>

- Given：<只違反一條規則，其他條件都符合>
- When：<同上>
- Then：HTTP <狀態>，`code = <錯誤碼>`；DB 不能被改動

## 規則檢查順序

同時違反多條規則時，回傳第一個不通過的：

1. <例如：資料存在（TCxx）>
2. <…>
