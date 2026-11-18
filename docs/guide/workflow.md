# 需求開發流程

收到需求（新增、修改、修正都算）用 `/requirement`；有進行中的需求要接著做，就從 `state.yaml` 的 `current` 與 `handoff` 接手。

每個 session 都要守的硬規則在 `CLAUDE.md`，這裡不重寫，只寫流程怎麼做。

## 建立需求

1. 跑 `./dev.sh in-progress`。有進行中的需求，依輸出的 `current` 與 `handoff` 告訴使用者它停在哪，問要先接著做還是另開；一次只做一個需求
2. 先看這包需求要不要拆成幾個需求（見下方「需求多大：拆需求與分步驟」）；要拆就先寫清單、等使用者確認，再開清單上的第一個
3. 取英文 kebab-case 的 slug，複製 `docs/changes/_template/` 到 `docs/changes/<slug>/`
4. 從 `main` 開這個需求自己的分支 `feat/<slug>`，之後每個階段的 commit 都在這個分支上；分支名寫進 `handoff.branch`
5. `requirement.md` 貼上原始需求原文，不改寫；`state.yaml` 填 `change`、`title`、`created`，`current: 1-clarify`；屬於某份清單就填 `plan`
6. 從釐清階段開始做

每個需求一個資料夾 `docs/changes/<slug>/`：

- `requirement.md`：原始需求、釐清問答、完成標準、範圍、決策
- `state.yaml`：`status`（需求的狀態）、`current`（目前階段）與 `handoff`（交接），AI 更新；有分步驟時加 `step`、`steps`，屬於清單時加 `plan`。各欄位怎麼寫看範本 `_template/state.yaml` 的註解
- `spec.approved`：使用者核准規格時產生的核准紀錄，**AI 不能建立或修改**
- `check-red.txt`、`check-green.txt`：`./dev.sh check` 在紅燈、綠燈階段自動存的檢查紀錄（實作前的紅燈、實作後的結果），**AI 不能手改**
- `red.md`：AI 對紅燈的分析

## 需求多大：拆需求與分步驟

三層：一包大需求拆成有順序的幾個需求；一個需求視情況照 feature 分步驟；一步裡面一次做完（不再分「先主線、後規則」）。

區分的關鍵是**能不能獨立交付、獨立測試**：

| | 拆成不同需求 | 同一個需求分步驟 |
|---|---|---|
| 什麼時候 | 每一塊做完可以自己合併，測試不需要另一塊存在（例：註冊 → 登入 → 忘記密碼） | 幾塊要一起才成立：規則要兩邊一致、有測案要跨兩塊（例：限時特價的後台設定、前台顯示） |
| 審查與合併 | 各自一次規格核准、各自合併；前一個合併了才開下一個 | 一次規格核准，全部做完一起合併 |
| 記在哪 | 清單 `docs/changes/_plan/<name>.md`，各需求的 `state.yaml` 寫 `plan` | 同一個 `state.yaml` 的 `step`、`steps` |

有順序不代表不能獨立：登入依賴註冊，註冊不依賴登入，所以是兩個需求照順序做。兩塊互相要對方才定得下來，才留在同一個需求。

### 拆成幾個需求：清單

- 清單寫在 `docs/changes/_plan/<name>.md`（範本 `_plan/_template.md`）：原始需求原文、需求清單（順序、slug、依賴、狀態）、跨需求的決定、不做的事
- 拆法與順序由 AI 提議，**使用者確認後**才開第一個需求；清單本身不是需求，沒有 1～7 這些階段
- 需求的資料夾輪到才建，一次還是只做一個
- 狀態只有三種：還沒開始、進行中、已合併。開那個需求時改「進行中」，使用者合併後改「已合併」並寫日期；做到哪個階段、第幾步不寫在清單，看那個需求的 `state.yaml`
- 做的時候定下、後面的需求也要照著做的事（例：帳號用 email、不分大小寫），寫進清單的「跨需求的決定」
- 接手有 `plan` 的需求，先讀清單

### 一個需求裡分步驟

- 一步是一個 feature：給同一種使用者用的一組 API。只有一個 feature 就不分步驟，不寫 `step`、`steps`
- 順序看誰依賴誰：寫資料的先、讀資料的後
- 共用的東西（改表、共用的判斷）放在最早用到它的那一步；跨 feature 的測案放最後一步
- 只有綠燈階段實作分步驟；其他階段都是整個需求一起做
- `state.yaml` 寫法：

  ```yaml
  current: 6-green
  step: 1                   # 現在做第幾步
  steps:                   # 釐清階段訂、規格核准回報時列出；之後要調整先問使用者
    1: 後台設定特價｜D1 D2 D3 D4 D5 D6 D7 D11 D12 D13
    2: 前台顯示特價｜D8 D9 D10
  ```

## 階段

一次只做 `current` 這一個階段。`current` 的值就是階段的編號加名稱：

| `current` | 做什麼 | 做完停下來嗎 | `status` |
|-----------|--------|--------------|----------|
| `1-clarify` | 釐清需求、訂完成標準 | 有問題就停 | `in_progress` |
| `2-spec` | 寫規格（OpenAPI、migration） | 不用 | `in_progress` |
| `3-cases` | 寫測案 | 不用，接著進規格核准 | `in_progress` |
| `4-approve` | 規格核准：人 review 規格與測案 | **一定停**，等使用者核准 | `waiting_review` |
| `5-red` | 寫測試，看到紅燈 | 不用 | `in_progress` |
| `6-green` | 實作到綠燈 | 不用 | `in_progress` |
| `7-report` | 回報（review 頁） | 停，問怎麼合併 | `in_progress`，符合「完成的定義」才改 `done` |

`current` 什麼時候改：**開始做一個階段時先把 `current` 改成那個階段**，上一個階段沒做完不要改。`./dev.sh check` 是照 `current` 決定檢查紀錄存到哪、缺測試算不算紅燈，改早了紀錄會存錯。

`status` 只有三個值，跟著階段走（上表最後一欄）：

- `in_progress`：AI 在做
- `waiting_review`：停在規格核准等使用者；只有這個狀態，對話輸入「規格通過」才會核准
- `done`：符合「完成的定義」。改成 `done` 之後 `./dev.sh in-progress` 就不再列出這個需求

### 1 釐清需求（`1-clarify`）

- 需求要有可驗證的結果（端點、預設值、錯誤情境）
- 商業規則未決的先問使用者，不自己假設
- 問答與完成標準（D1、D2…）寫進 `requirement.md`
- 使用者也可以明說「照你的判斷」，記進「決策」
- 完成標準涵蓋不只一個 feature 時訂分步驟：`state.yaml` 寫 `steps`（每一步的名稱與包含哪幾條完成標準），`step: 1`
- 釐清後發現其中一塊可以獨立交付、獨立測試，先提議拆成另一個需求，使用者決定

### 2 規格（`2-spec`）

- 把釐清階段定下來的端點、欄位、錯誤情境寫成正式規格
- 要改表就新增 `db/migration/V<n>__*.sql`
- `docs/api/openapi.yaml` 補上 path、request、每個會回的 HTTP 狀態

### 3 寫測案（`3-cases`）

- 測案是 API 行為案例（文字），不是測試程式；測試程式在紅燈階段寫
- 寫在 `docs/test-cases/<feature>.md`（範本 `_template.md`）
- 每案 `### TCxx`：Given / When / Then、HTTP 狀態與 `code`；路徑、欄位、`code` 用規格階段定下的名稱
- 測案對的是 `requirement.md` 的完成標準（D1、D2…），不是照規格列一遍；每條完成標準都要有測案，編號回填到「對應檢查」
- 寫測案時發現規格少了狀態或錯誤情境，回頭補 `openapi.yaml`
- 新的 feature 在 `feature_list.json` 加一筆（見下方「功能清單」），`status` 寫 `not_started`
- 失敗案例要寫「DB 不能被改動」

### 4 規格核准（`4-approve`）

- `status` 改 `waiting_review`
- 回報完成標準對照測案、改了哪些檔；有分步驟就列出每一步包含哪些完成標準與順序，然後停下來等使用者核准
- 使用者怎麼核准：在對話單獨輸入「規格通過」（整則訊息只有這句才算，也接受「規格通過，繼續」），UserPromptSubmit hook（`scripts/spec-approve.sh`）會替 `status` 是 `waiting_review` 的需求寫入 `spec.approved`；或在終端機執行 `./dev.sh approve`
- 規格通過後（`./dev.sh in-progress` 顯示規格核准：approved）：`status` 改回 `in_progress`，進紅燈階段
- 核准後規格（測案、OpenAPI、migration）又變動，核准會失效（`./dev.sh in-progress` 顯示 stale）：`status` 改回 `waiting_review`，請使用者重新核准
- 這是機制擋住的關卡：有進行中的需求而規格沒核准時，PreToolUse hook（`scripts/spec-gate.sh`）不讓 AI 改 `src/`

### 5 寫測試、紅燈（`5-red`）

- fixture 放 `src/test/resources/fixtures/<feature>/TCxx_*.sql`
- `<Feature>ApiTest` 一案一方法，方法名 `tcNN_` 開頭
- `./dev.sh check`（不帶參數）看到紅燈；`current` 是 `5-red` 時，紅燈紀錄會自動存成 `docs/changes/<slug>/check-red.txt`（總數、一個測試一行、失敗訊息原文），**不要手改、不要自己抄一份**
- 紅燈階段跑出綠燈會有 WARNING：新測試沒有紅過，先查測試是不是真的有在驗東西，不要直接進綠燈階段
- 分析寫進 `docs/changes/<slug>/red.md`，只寫 `check-red.txt` 看不出來的事：紅的原因是什麼；不是「功能還沒做」的（編譯錯、fixture 載入失敗、實作後仍會紅）要另外標出來。總數與逐案結果不用重抄，引用 `check-red.txt`
- `check-red.txt` 存好、`red.md` 寫好，這個階段才算做完（`current` 改成 `6-green` 之後的紅燈不會再存進 `check-red.txt`）

### 6 實作到綠燈（`6-green`）

- 實作到 `./dev.sh check`（不帶參數）全綠（含測案都有測試、沒有測試被略過）；只跑一支的 `-Dtest=` 不算完整檢查
- `current` 是 `6-green` 時，每次完整檢查的紀錄會自動存成 `docs/changes/<slug>/check-green.txt`，最後一次就是實作完的結果
- `check-red.txt` 與 `check-green.txt` 的 `test_hash` 不一樣，代表紅燈之後 `src/test` 被改過：在回報裡說明改了什麼、為什麼
- 有分步驟：照 `steps` 的順序一步一步實作，只改這一步需要的程式。前面幾步做完時整體檢查還是紅的（後面的步驟還沒實作），所以要逐個對 `.run/check-last.txt`：這一步完成標準對應的測試全綠、原本就綠的測試沒有變紅、紅的只剩後面的步驟的，才把 `step` 加 1，並在對話回報一句（不用停）。最後一步做完要完整檢查全綠
- 最後一次完整檢查全綠、`check-green.txt` 存好，這個階段才算做完

### 7 回報（`7-report`）

回報寫成 review 頁 `docs/changes/<slug>/review.html`，人在瀏覽器看回報、親手試。照這個順序做：

1. 寫 `review.html`（做法見下方「review 頁」）
2. `./dev.sh app-restart`（會印出 review 頁網址），服務留著不關
3. AI 用瀏覽器打開：確認各段都有讀到資料，按「全部依序執行」要全部符合預期，再按一次確認能重跑。有步驟不符預期，先判斷是步驟寫錯還是功能有問題；功能有問題就回綠燈階段，不要改步驟去配合
4. 照「完成的定義」逐項確認；都符合才把 `status` 改 `done`，少一項就不改，照實回報
5. 更新 `handoff`，commit，跑 `./dev.sh clean-check`
6. 在對話回報，停下來問使用者怎麼合併。對話裡只回：一句結論、review 頁網址、AI 實跑親手試的結果、需要使用者判斷的事有幾件；不要把 review 頁的內容再貼一次

屬於清單的需求：使用者合併後，把清單上這個需求的狀態改「已合併」，並告訴使用者清單上的下一個是什麼。

#### review 頁

一個需求一頁，由上到下：結論、需要你判斷的事、完成標準對照、親手試、改了什麼、上線與範圍、檢查紀錄原文。畫面與程式共用 `docs/changes/_review/review.js`、`review.css`；`review.html` 只寫內容（從 `docs/changes/_review/review.template.html` 複製，欄位說明寫在 `review.js` 開頭）。

已經寫在別處的內容由頁面自己讀檔，**不要抄進 `review.html`**：

- 測試數、逐案結果、實作前的紅燈、hash 對照：讀 `check-green.txt`、`check-red.txt`、`spec.approved`
- 完成標準與對應測案、決策、不做的事：讀 `requirement.md`（所以這三段的標題與表格格式照範本，不要改）
- migration 的 SQL：`migrations` 填檔名，頁面讀 `db/migration/`

AI 要寫的：

- `summary`：做了什麼，兩三句
- `attention`（需要你判斷的事）：核准後規格或測試的變動（改了什麼、為什麼）、沒辦法驗的完成標準、只有自動測試涵蓋的情境、實作前一開始就綠的新測試。決策不用重寫，頁面會列 `requirement.md` 的決策表
- `apiChanges`：既有 API 的回應或行為變了什麼（舊畫面、舊呼叫端會不會受影響）
- `changes`：改的檔案分組列出，每個檔一句說明
- `deploy`：上線要注意的事；`findings`：範圍外發現、沒動手的事
- `groups`：親手試的步驟

親手試是讓人照步驟點、看實際回應，不是把測案再跑一遍（那是自動測試的事）：

- 照完成標準走一遍，加幾個代表性的失敗情境；每步寫 `covers`（對應哪幾條完成標準）、預期的 HTTP 狀態與 `code`，重點欄位寫成逐項檢查
- 資料用 `db/seed.sql` 現成的資料加上頁面自己打 API 準備，不另外對 DB 塞資料；開頭放「還原」步驟，讓整頁不用重啟也能重跑
- 時間寫死成很遠的日期（期間內 2020～2099、已過期 2020 年、還沒開始 2099 年），不寫相對現在的時間
- API 準備不出來的情境（沒有端點能做出的狀態、「DB 不能被改動」）不放進步驟，寫進 `attention` 的「只有自動測試」

## 完成的定義（Definition of Done）

前五項都符合才能把 `status` 改 `done`；改完連同 `state.yaml` 一起 commit、收工檢查通過（最後一項），才能說做完。少一項就照實回報哪一項沒做到，不要說做完了。

- [ ] `requirement.md` 每條完成標準的「對應檢查」都填了（測案編號、`ArchitectureTest` 規則，或「人工確認」加理由）
- [ ] 最後一次 `./dev.sh check`（不帶參數）全綠，紀錄存在 `check-green.txt`
- [ ] `feature_list.json` 裡這個需求的功能是 `passing`（`./dev.sh check` 更新的，不是手改的）
- [ ] 核准後規格或測試有變動的（`check-red.txt` 與 `check-green.txt` 的 `test_hash` 不同、規格重新核准過），在 review 頁說明了改什麼、為什麼
- [ ] `review.html` 寫好，AI 在瀏覽器按「全部依序執行」全部符合預期，而且能重跑
- [ ] 回報階段的變更（含改成 `done` 的 `state.yaml`）已 commit，`./dev.sh clean-check` 是 `CLEAN: PASS`

## 功能清單 `feature_list.json`

全部功能一份清單，新 session 先看它就知道有哪些功能、各自的狀態、怎麼驗。

- 一筆一個功能，`id` 與 `docs/test-cases/<id>.md` 檔名相同；`dependencies` 寫這個功能要先有哪些功能
- `verification` 指向測案檔，`evidence` 是測試類別名稱：驗證步驟只寫在測案，不抄進清單
- `status` 由 `./dev.sh check`（不帶參數）依測試結果更新，**不要手改**：`not_started`（還沒有測試）→ `active`（有測試、還沒全綠）→ `passing`（每個測案都有測試而且全綠）
- 測案階段寫測案時加一筆；有測案沒登記，`./dev.sh check` 會紅燈

## 每個階段都一樣的做法

硬規則（動手前先檢查、範圍外不動手、不 push 不合併）在 `CLAUDE.md`；這裡是照著做時的細節。

- 測案或規格沒寫到、自己做的選擇，記進 `requirement.md` 的「決策」，之後的 session 照著做
- 不能為了讓測試過關而改測試或測案；覺得測案有問題，問使用者
- 每個階段做完 commit 一次，`state.yaml` 的更新一起 commit；訊息寫 `<slug> <階段>：做了什麼`，例如 `product-list 5-red：寫測試`
- 每一輪結束更新 `state.yaml`：`current`（有分步驟時還有 `step`）與 `handoff` 四個欄位（怎麼寫看 `state.yaml` 範本的註解）
- 更新完跑 `./dev.sh clean-check`（收工檢查，項目與處理方式見 [clean-state-checklist.md](clean-state-checklist.md)）
