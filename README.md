# ai-tdd-demo

示範 harness engineering：讓 AI（Claude Code）在本機安全的環境裡，照流程做需求。題目是電商後台的商品管理（Java 17、Spring Boot 3、MySQL）。

## 需要

Docker Desktop、JDK 17、Maven、Claude Code

## 起環境

```bash
./dev.sh db-up       # 第一次會產生 .env、起 MySQL、套用 migration、載入 seed
./dev.sh check       # 重建環境並跑全部測試，應該全綠
./dev.sh app-start   # 起服務 http://localhost:8090
./dev.sh api GET /api/admin/products/1
```

`./dev.sh` 不帶參數會列出所有命令。

## harness 的五個部分

| 子系統 | 檔案 | 做什麼 |
|--------|------|--------|
| 指令 | `CLAUDE.md`、`.claude/skills/`、`docs/guide/` | `CLAUDE.md` 只放硬規則與目錄，細節按需去讀 |
| 工具 | `dev.sh`、`.claude/settings.json` | AI 只能走 `./dev.sh`；直接下 docker、mysql、curl、讀 `.env` 被擋 |
| 環境 | `docker-compose.yml`、`db/migration/` | 本機 MySQL，隨時清空重建；表結構只能寫成 .sql |
| 狀態 | `docs/changes/<slug>/state.yaml`、`feature_list.json` | `current`（做到哪）與 `handoff`（交接），換 session 也能接手；功能清單記全部功能與狀態，由 `./dev.sh check` 更新 |
| 回饋 | `ArchitectureTest`、OpenAPI 驗證、規格核准 hook、`./dev.sh clean-check` | 慣例與規格寫成會紅燈的測試；規格沒核准時 hook 不讓 AI 改 `src/`；收工檢查確認下一個 session 接得起來 |

## 需求流程

```
1 釐清 → 2 規格 → 3 測案 → 4 規格核准（人 review，對話輸入「規格通過」）→ 5 紅燈 → 6 綠燈 → 7 回報
```

每個需求一個資料夾 `docs/changes/<slug>/`，用 `/requirement` 建立，換 session 從 `state.yaml` 接手。階段與規則在 `docs/guide/workflow.md`。

## demo 流程

1. `./dev.sh db-up`，確認 `./dev.sh check` 綠燈
2. 開 Claude Code，輸入 `/requirement` 加一段需求，例如：「後台營運人員把草稿商品上架，上架前要確認資料完整、價格與庫存合理、分類可用」
3. AI 在釐清階段問問題，寫測案與 OpenAPI，停在規格核准。此時要它直接寫程式，改 `src/` 會被 hook 擋下
4. 在對話單獨輸入「規格通過」（hook 寫入 `spec.approved`；也可以在終端機執行 `./dev.sh approve`）
5. AI 寫測試（紅燈）、實作（綠燈）、回報
6. 現場在測案加一個 TC，說「測案新增了，繼續」，看 AI 補測試與實作
