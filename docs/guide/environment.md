# 環境與 DB

## 環境操作：只用 ./dev.sh

所有環境操作都透過 `./dev.sh`，**不要直接下 `docker`、`mysql`、`curl`、`mvn`**。完整命令與說明：`./dev.sh`（不帶參數）。最常用的：

| 要做的事 | 命令 |
|----------|------|
| 開工前、每次改完 | `./dev.sh check`（`db-up` 重建環境，再跑全部測試；只跑一支：`./dev.sh check -Dtest=XxxApiTest`，不算完整檢查）。結果寫在 `.run/check-last.txt`；紅燈時的請求與回應在 `target/surefire-reports/TEST-*.xml` |
| 起服務、打 API、查 DB | `./dev.sh app-start`、`./dev.sh api POST /api/...`、`./dev.sh sql "SELECT ..."` |
| 看進行中的需求 | `./dev.sh in-progress` |
| 每一輪結束前 | `./dev.sh clean-check`（收工檢查，見 [clean-state-checklist.md](clean-state-checklist.md)） |

- Docker 沒開時 `./dev.sh` 會提示，請使用者打開 Docker Desktop，不要自己嘗試啟動
- 現有命令做不到的事不要繞路（例如直接 docker exec 或改 docker-compose.yml）：說明缺什麼，提議在 `dev.sh` 加命令，由使用者決定
- 環境工具檔由使用者維護，AI 不能改：`scripts/`、`docker-compose.yml`、`db/init/`。要改就說明改什麼、為什麼。`dev.sh` 和 `.claude/settings.json` 使用者要求時可以改
- 連線資訊（port、密碼）放在 `.env`，由 `dev.sh` 讀取；AI 不需要也不要讀 `.env`

## DB

### 資料庫與帳號

| 項目 | 內容 |
|------|------|
| 位置 | 本機 docker 的 MySQL，schema 名稱 `shop` |
| 用途 | AI 開發與驗證專用，資料不保留，隨時清空重建 |
| 不在範圍內 | 人的測試環境、正式環境：由其他團隊維護，AI 不連、不管 |
| `app_user` | 服務、測試、`./dev.sh sql` 都用它，**只有 DML** |
| `migrator` | 只有 `./dev.sh` 用來套用 `db/migration/*.sql`；服務拿不到 |

### 清空重建與資料分層

- `db-up`、`check` 都會清空重建 `shop`（依檔名順序套用全部 migration），不用先問使用者
- 資料分兩層，id 範圍不重疊：`db/seed.sql`（id 1～99，`db-up` 載入，給服務與打 API 用）、測試 fixture（id 100 以上，每個測試前先清空，只放該案例的資料）
- 服務在跑時執行 `./dev.sh check`，服務的資料也會被清掉

## DB 結構變更怎麼套用

要不要改表、改什麼，是需求的事（requirement.md、規格階段）。這裡只講機制：表結構的任何變更都寫成 `.sql` 檔，不能直接對 DB 下 DDL。沒有 Flyway、沒有版本記錄；上線時怎麼套用由人自己處理。服務本身不會 migrate。

- 放 `db/migration/V<下一個版本號>__<簡述>.sql`，例如 `V2__add_product_brand.sql`
- 內容是異動語法（`ALTER TABLE ...`、`CREATE TABLE ...`），只寫這次的變更
- 還沒 commit 的 .sql 寫錯了，直接改檔再跑 `./dev.sh db-up` 或 `./dev.sh check`（會整個重建）
- 已 commit 的 .sql 不要改（人可能已經套用到其他環境）：要修正就新增一版，並在回報中說明
- `./dev.sh app-start` 不會套用 .sql；改了檔先 `./dev.sh db-up`
- 不要在 `src/main/resources` 放 .sql，也不要在服務加 Flyway 依賴或 `spring.flyway` 設定
