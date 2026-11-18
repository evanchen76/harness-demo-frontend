# ai-tdd-demo

電商後台（商品管理）。Java 17、Spring Boot 3、MyBatis-Plus、MySQL 8.4（本機 docker）。

這份檔案只放每個 session 都必須遵守的硬規則與文件目錄，細節在 `docs/guide/`。

## 硬規則

- 所有環境操作只用 `./dev.sh`（不帶參數印出完整命令），**不要直接下 `docker`、`mysql`、`curl`、`mvn`**；現有命令做不到的事不要繞路，提議在 `dev.sh` 加命令，由使用者決定。Docker 沒開請使用者打開，不要自己啟動
- AI 不能改的環境工具：`scripts/`、`docker-compose.yml`、`db/init/`（要改就說明改什麼、為什麼，讓使用者改）
- AI 不能建立或手改 `spec.approved`（使用者在對話輸入「規格通過」或執行 `./dev.sh approve` 產生），也不能執行 `scripts/spec-approve.sh`
- 不要讀 `.env`
- 表結構變更只能寫成 `db/migration/V<n>__*.sql`，不能直接對 DB 下 DDL
- 動手前先跑 `./dev.sh check`；**紅燈先停下來回報，不要動功能程式碼**（紅燈、綠燈階段新測試的紅燈是預期內）
- 一次只做一個需求的 `current` 階段；範圍外想改的事寫在回報裡，不動手
- 做完才能說做完：照 `docs/guide/workflow.md`「完成的定義」逐項確認，少一項就照實回報
- 每一輪結束更新 `state.yaml` 的 `current` 與 `handoff`，再跑 `./dev.sh clean-check`
- `feature_list.json` 的 `status` 由 `./dev.sh check` 更新，不要手改
- git：每個階段做完 commit 一次；使用者要求才 push，不要合併進 main

## 文件目錄

| 想知道 | 看哪裡 |
|--------|--------|
| 環境怎麼操作、DB 與帳號規則、怎麼改表結構 | [docs/guide/environment.md](docs/guide/environment.md) |
| 需求怎麼做、接著做：階段 1～7、交接、完成的定義；大需求怎麼拆、怎麼分步驟 | [docs/guide/workflow.md](docs/guide/workflow.md) |
| 每一輪結束前要檢查什麼 | [docs/guide/clean-state-checklist.md](docs/guide/clean-state-checklist.md) |
| 有哪些功能、各自的狀態、怎麼驗 | `feature_list.json` |
| 程式放哪裡、程式慣例（`ArchitectureTest` 檢查的規則） | [docs/guide/architecture.md](docs/guide/architecture.md) |
| 開需求（新增、修改、修正） | `/requirement`（`.claude/skills/`） |
| API 規格、測案 | `docs/api/openapi.yaml`、`docs/test-cases/<feature>.md` |
| 進行中的需求 | `./dev.sh in-progress`、`docs/changes/<slug>/` |
