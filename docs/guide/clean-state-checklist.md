# 收工檢查（clean state）

每一輪結束前跑 `./dev.sh clean-check`。目的：下一個 session 什麼都不記得，也能從 repo 裡的檔案直接接手。

## 程式檢查的（`./dev.sh clean-check`）

`✗` 要處理完才收工；「提醒」是給使用者看的現況，照實寫進回報。

| 項目 | 沒過怎麼辦 |
|------|------------|
| 需求資料夾都有 `state.yaml` | 空的或沒建完的資料夾：補完或刪掉 |
| 進行中的需求最多一個，`handoff.updated` 是今天 | 更新 `current` 與 `handoff` |
| 最後一次完整檢查之後，規格、測試、程式都沒再改 | 再跑一次 `./dev.sh check`（不帶參數） |
| 最後一次完整檢查是綠燈 | 紅燈、綠燈階段的紅燈是預期內，`handoff.note` 寫清楚紅在哪；其他階段紅燈先回報，不能留著收工 |
| 每份測案都登記在 `feature_list.json` | 補一筆，`status` 寫 `not_started` |
| 提醒：還沒 commit 的檔 | 階段做完就 commit；做到一半就在 `handoff.note` 寫清楚 |
| 提醒：`.run/` 裡不是 `dev.sh` 產生的檔 | 用不到就刪掉 |

## 程式檢查不到、要自己確認的

- `handoff.note` 只寫別處看不出來的事：等使用者回答什麼、試過什麼為什麼放棄
- `handoff.next` 寫得出下一步具體要做什麼（哪個檔、哪個測案），不是「繼續做」
- 這一輪做的選擇已經記進 `requirement.md` 的「決策」
- 範圍外發現的事寫在回報裡，沒有動手改
