#!/usr/bin/env bash
# 本機開發與 AI 驗證用的唯一操作入口。AI 只透過這支腳本操作環境，不直接下 docker。
# 這段說明是命令的唯一完整清單（./dev.sh 不帶參數會印出來）
#
# 環境（MySQL 在本機 docker；結構由 db/migration/*.sql 套用）
# 只有一個庫 shop，給 AI 驗證用、資料不保留：db-up、check 都會清空重建
#   ./dev.sh db-up              起 MySQL，清空 shop、依序套用 db/migration 全部 .sql、載入 seed
#   ./dev.sh db-down            停 MySQL
#   ./dev.sh sql "<SQL>"        以 app_user（只有 DML）對 shop 執行 SQL
#
# 檢查
#   ./dev.sh check [mvn 參數]    開工前與每次改完都跑：db-up 重建環境，跑全部測試，再對照測案都有測試；紅燈就先停下來回報
#                               有測試被略過（@Disabled）也算紅燈
#                               只跑一支測試：./dev.sh check -Dtest=ProductOnShelfApiTest（不算完整檢查）
#                               每次都把結果寫進 .run/check-last.txt（一個測試一行、失敗訊息原文）；
#                               紅燈、綠燈階段（current 是 5-red、6-green）的完整檢查另存一份到 docs/changes/<slug>/check-red.txt、check-green.txt
#                               完整檢查會依測試結果更新 feature_list.json 每個功能的 status（不要手改 status）
#   ./dev.sh clean-check        每一輪結束前跑（收工檢查）：需求資料夾完整、交接有更新、最後一次完整檢查對得上現在的檔案、
#                               功能清單沒有漏登記；另外列出還沒 commit 的檔與 .run 裡的殘留檔
#
# 服務（Spring Boot，http://localhost:8090）
#   ./dev.sh app-start          打包並在背景啟動服務；啟動後列出 review 頁網址
#                               （docs/changes/<slug>/review.html，瀏覽器開 http://localhost:8090/try/<slug>/review.html）
#   ./dev.sh app-stop           停止服務；改了程式要先 app-stop 再 app-start
#   ./dev.sh app-restart        改完要試新版時用：停服務 → 清空重建 DB（套用全部 migration、載入 seed）→ 重新打包啟動
#   ./dev.sh api <METHOD> <path> [JSON body]
#                               打本機服務的 API（只會打 localhost），印出回應與 HTTP 狀態
#
# 需求流程
#   ./dev.sh in-progress        列出還沒完成的需求：目前階段、規格核准狀態、交接（handoff）
#   ./dev.sh approve [slug]     【只有人能執行，AI 被 settings 擋掉】規格核准，寫入 docs/changes/<slug>/spec.approved；在對話單獨輸入「規格通過」也會執行它
set -euo pipefail
cd "$(dirname "$0")"

APP_PORT=8090
RUN_DIR=.run
PID_FILE=$RUN_DIR/app.pid
LOG_FILE=$RUN_DIR/app.log
JAR=target/ai-tdd-demo-0.0.1-SNAPSHOT.jar

die() { echo "ERROR: $*" >&2; exit 1; }

# ---- 環境設定 ----------------------------------------------------------------

init_env() {
  [ -f .env ] && return
  rnd() { LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 20; }
  cat > .env <<EOF
DB_PORT=3317
DB_ROOT_PASSWORD=$(rnd)
MIGRATOR_DB_PASSWORD=$(rnd)
APP_DB_PASSWORD=$(rnd)
EOF
  chmod 600 .env
  echo "已產生 .env（本機隨機密碼）"
}

load_env() {
  init_env
  set -a; . ./.env; set +a
}

use_jdk17() {
  local home
  home=$(/usr/libexec/java_home -v 17 2>/dev/null) || die "找不到 JDK 17"
  export JAVA_HOME=$home
}

dc() { docker compose "$@"; }

require_docker() {
  docker info >/dev/null 2>&1 || die "Docker 沒有在跑，請使用者先打開 Docker Desktop"
}

require_db() {
  [ "$(docker inspect -f '{{.State.Health.Status}}' ai-tdd-demo-mysql 2>/dev/null || true)" = healthy ] \
    || die "MySQL 沒有起來，請先執行 ./dev.sh db-up"
}

# 密碼只在 container 內展開，不會出現在指令列或輸出
mysql_root()     { dc exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --default-character-set=utf8mb4 "$@"' -- "$@"; }
mysql_app()      { dc exec -T mysql sh -c 'MYSQL_PWD="$APP_DB_PASSWORD" exec mysql -uapp_user --default-character-set=utf8mb4 "$@"' -- "$@"; }
mysql_migrator() { dc exec -T mysql sh -c 'MYSQL_PWD="$MIGRATOR_DB_PASSWORD" exec mysql -umigrator --default-character-set=utf8mb4 "$@"' -- "$@"; }

# shop 只給 AI 驗證用，資料不保留：刪庫重建，再依檔名順序套用 db/migration 全部 .sql
shop_reset() {
  mysql_root -e "DROP DATABASE IF EXISTS shop; CREATE DATABASE shop CHARACTER SET utf8mb4;"
  local f
  for f in db/migration/*.sql; do
    [ -f "$f" ] || continue
    mysql_migrator shop < "$f" || die "$f 套用失敗（看上面的錯誤，改檔後再跑一次）"
    echo "shop: applied $f"
  done
}

# ---- 環境與檢查 ---------------------------------------------------------------

cmd_up() {
  require_docker; load_env
  dc up -d --wait
  shop_reset
  mysql_root shop < db/seed.sql
  echo "shop: seed loaded"
}

cmd_down() { require_docker; load_env; dc stop; }

cmd_sql() {
  [ -n "${1:-}" ] || die "用法: ./dev.sh sql \"<SQL>\""
  require_docker; load_env; require_db
  mysql_app -t shop -e "$1"
}

# 測案對照：docs/test-cases/<feature>.md 的每個 ### TCxx，都要有 <Feature>ApiTest 的 tcxx_ 方法
# 綠燈、回報階段（6-green、7-report）缺測試算紅燈；更早的階段只提醒
cases_check() {
  local md b cls t tc missing=0 cur=""
  for md in docs/test-cases/*.md; do
    b=$(basename "$md" .md); [ "$b" = _template ] && continue
    cls=$(echo "$b" | awk -F- '{for (i = 1; i <= NF; i++) printf "%s", toupper(substr($i, 1, 1)) substr($i, 2)}')ApiTest
    t=$(find src/test -name "$cls.java" | head -1)
    for tc in $(grep -o '^### TC[0-9]*' "$md" | sed 's/### //' | tr 'A-Z' 'a-z'); do
      if [ -z "$t" ] || ! grep -q "void ${tc}_" "$t"; then
        echo "  → 測案 $b $tc 沒有對應的測試（$cls 要有 ${tc}_ 開頭的方法）"; missing=1
      fi
    done
  done
  [ $missing = 0 ] && return 0
  local f; f=$(active_states | head -1); [ -n "$f" ] && cur=$(state_get "$f" current)
  case "$cur" in 6-green|7-report) return 1 ;; esac
  echo "  （目前階段還不是實作，只提醒）"
  return 0
}

# ---- 功能清單 feature_list.json ------------------------------------------------
# 一筆一個功能：id 與 docs/test-cases/<id>.md 的檔名相同，evidence 是測試類別名稱。
# status 由完整檢查依測試結果更新：not_started（還沒有測試）/ active（有測試、還沒全綠）/ passing（每個測案都有測試而且全綠）
FEATURE_LIST=feature_list.json

# 每份測案都要登記在 feature_list.json
features_registered() {
  command -v jq >/dev/null || die "需要 jq（讀寫 ${FEATURE_LIST}）"
  [ -f "$FEATURE_LIST" ] || { echo "  → 找不到 ${FEATURE_LIST}"; return 1; }
  local md b missing=0
  for md in docs/test-cases/*.md; do
    b=$(basename "$md" .md); [ "$b" = _template ] && continue
    jq -e --arg id "$b" 'any(.features[]; .id == $id)' "$FEATURE_LIST" >/dev/null 2>&1 \
      || { echo "  → 測案 $b 沒有登記在 ${FEATURE_LIST}（寫測案時加一筆，status 寫 not_started）"; missing=1; }
  done
  return $missing
}

# $1 是 test_results 的輸出。沒有測試報告（編譯失敗）時不動，免得全部被改成 not_started
features_update() {
  [ -f "$FEATURE_LIST" ] && [ -n "$1" ] || return 0
  command -v jq >/dev/null || return 0
  local id cls md all bad tc st new tab
  tab=$(printf '\t')
  new=$(cat "$FEATURE_LIST")
  for id in $(jq -r '.features[].id' "$FEATURE_LIST"); do
    cls=$(jq -r --arg id "$id" '.features[] | select(.id == $id) | .evidence' "$FEATURE_LIST")
    md=docs/test-cases/$id.md
    all=$(echo "$1" | grep -c "${tab}${cls}\." || true)
    bad=$(echo "$1" | grep "${tab}${cls}\." | grep -vc "^PASS" || true)
    if [ "$all" = 0 ]; then st=not_started
    elif [ "$bad" != 0 ]; then st=active
    else
      st=passing
      [ -f "$md" ] || st=active
      for tc in $(grep -o '^### TC[0-9]*' "$md" 2>/dev/null | sed 's/### //' | tr 'A-Z' 'a-z'); do
        echo "$1" | grep -q "^PASS${tab}${cls}\.${tc}_" || st=active
      done
    fi
    new=$(echo "$new" | jq --arg id "$id" --arg st "$st" '(.features[] | select(.id == $id) | .status) = $st')
  done
  if [ "$new" != "$(cat "$FEATURE_LIST")" ]; then
    echo "$new" > "$FEATURE_LIST"
    echo "  → 已依測試結果更新 ${FEATURE_LIST} 的 status"
  fi
}

# ---- check 紀錄 ---------------------------------------------------------------
REPORT_DIR=target/surefire-reports
CHECK_RECORD=$RUN_DIR/check-last.txt

# 一個路徑底下所有檔案（含未 commit、不含 .gitignore 的）的內容 hash：兩份紀錄的值一樣，代表這些檔案沒變
tree_hash() {
  local f
  git ls-files -co --exclude-standard "$@" | LC_ALL=C sort | while read -r f; do
    [ -f "$f" ] && echo "$f $(git hash-object "$f")"
  done | git hash-object --stdin
}

# 從 surefire 的 XML 整理出一個測試方法一行：結果<TAB>類別.方法<TAB>訊息
# 結果是 PASS / FAIL（斷言不成立）/ ERROR（丟例外）/ SKIP；訊息照原文，換行暫時換成 \001
test_results() {
  ls "$REPORT_DIR"/TEST-*.xml >/dev/null 2>&1 || return 0
  awk '
    function decode(s) {
      gsub(/&#10;/, "\001", s); gsub(/&quot;/, "\"", s); gsub(/&apos;/, "\047", s)
      gsub(/&lt;/, "<", s); gsub(/&gt;/, ">", s); gsub(/&amp;/, "\\&", s)
      return s
    }
    function attr(line, name,   s, i) {
      i = index(line, name "=\""); if (i == 0) return ""
      s = substr(line, i + length(name) + 2)
      return substr(s, 1, index(s, "\"") - 1)
    }
    function flush() { if (id != "") print st "\t" id "\t" msg; id = "" }
    /^  <testcase / {
      flush()
      cls = attr($0, "classname"); sub(/.*\./, "", cls)
      id = cls "." attr($0, "name"); st = "PASS"; msg = ""
    }
    /^    <failure[ >]/ { st = "FAIL";  msg = decode(attr($0, "message")) }
    /^    <error[ >]/   { st = "ERROR"; msg = decode(attr($0, "message")) }
    /^    <skipped[ \/>]/ { st = "SKIP"; msg = decode(attr($0, "message")) }
    END { flush() }
  ' "$REPORT_DIR"/TEST-*.xml | LC_ALL=C sort -t "$(printf '\t')" -k2,2
}

# 把這次 check 的結果寫成紀錄檔。$1 結果（PASS / FAIL）、$2 範圍、$3 test_results 的輸出
write_check_record() {
  local n_pass n_fail n_err n_skip
  n_pass=$(echo "$3" | grep -c "^PASS" || true)
  n_fail=$(echo "$3" | grep -c "^FAIL" || true)
  n_err=$(echo "$3" | grep -c "^ERROR" || true)
  n_skip=$(echo "$3" | grep -c "^SKIP" || true)
  mkdir -p "$RUN_DIR"
  {
    echo "# 由 ./dev.sh check 產生，不要手改。失敗訊息是 surefire 報告的原文"
    echo "time: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "result: $1"
    echo "scope: $2"
    echo "total: 通過 ${n_pass}、失敗 ${n_fail}、錯誤 ${n_err}、略過 ${n_skip}"
    echo "commit: $(git rev-parse --short HEAD)"
    echo "spec_hash: $(spec_hash)        # 測案、OpenAPI、migration"
    echo "test_hash: $(tree_hash src/test)        # src/test 全部檔案"
    echo "main_hash: $(tree_hash src/main)        # src/main 全部檔案"
    echo
    [ -n "$3" ] || echo "（沒有測試報告：編譯失敗或 Spring 起不來）"
    echo "$3" | awk -F '\t' 'NF {
      printf "%-5s %s\n", $1, $2
      if ($3 != "") { gsub(/\001/, "\n        ", $3); print "        " $3 }
    }'
  } > "$CHECK_RECORD"
}

# 進行中的需求在紅燈、綠燈階段，把完整檢查的紀錄留一份在需求資料夾（進 git）：
#   check-red.txt 只在紅燈時寫（實作前的紅燈）；check-green.txt 每次都寫（最後一次就是實作完的結果）
keep_stage_record() {
  local f cur dir
  f=$(active_states)
  [ -n "$f" ] && [ "$(echo "$f" | wc -l | tr -d ' ')" = 1 ] || return 0
  cur=$(state_get "$f" current); dir=$(dirname "$f")
  case "$cur" in
    5-red) if [ "$1" = FAIL ]; then cp "$CHECK_RECORD" "$dir/check-red.txt"; echo "  → 紀錄已存 $dir/check-red.txt"
        else echo "  WARNING: 目前是 5-red（寫測試、紅燈）卻是綠燈：新測試沒有紅過，先確認測試真的有在驗東西"; fi ;;
    6-green) cp "$CHECK_RECORD" "$dir/check-green.txt"; echo "  → 紀錄已存 $dir/check-green.txt" ;;
  esac
}

# check：先 db-up 把環境重建好，再跑測試（ArchitectureTest 加各 API 測試）、對照測案、寫紀錄
cmd_check() {
  local scope="全部"
  [ $# -gt 0 ] && scope="部分（$*）"
  cmd_up
  use_jdk17
  # 測試前再清空一次 shop，每次都從空表開始跑
  shop_reset
  rm -rf "$REPORT_DIR"
  local rc=0 results n_skip n_all
  mvn -B -ntp -q test "$@" || rc=$?
  results=$(test_results)
  n_all=$(echo "$results" | grep -c . || true)
  n_skip=$(echo "$results" | grep -c "^SKIP" || true)
  if [ -n "$results" ]; then
    echo "---- 測試結果 ----"
    grep -h "Tests run" "$REPORT_DIR"/*.txt | sed 's/ -- in / /'
  fi
  [ $# -gt 0 ] || features_update "$results"
  if [ $rc = 0 ] && [ "$n_skip" != 0 ]; then
    echo "  → 有 $n_skip 個測試被略過（@Disabled 等），略過算紅燈："
    echo "$results" | awk -F '\t' '$1 == "SKIP" { print "     " $2 }'
    rc=1
  fi
  if [ $rc = 0 ]; then
    cases_check || rc=1
    features_registered || rc=1
  fi
  if [ $rc = 0 ]; then
    write_check_record PASS "$scope" "$results"
    if [ $# -gt 0 ]; then
      echo "CHECK: PASS（只跑部分：$*，$n_all 個測試；不算完整檢查，完整檢查請不帶參數）"
    else
      echo "CHECK: PASS（綠燈）全部 $n_all 個測試"
      keep_stage_record PASS
    fi
    echo "  → 紀錄：$CHECK_RECORD"
    return 0
  fi
  write_check_record FAIL "$scope" "$results"
  echo "CHECK: FAIL（紅燈）$scope"
  echo "$results" | awk -F '\t' '$1 == "FAIL" || $1 == "ERROR" { print "  " $1 " " $2 }'
  echo "  → 每個測試的結果與失敗訊息：$CHECK_RECORD"
  local f
  for f in $(grep -lE "<failure|<error" "$REPORT_DIR"/TEST-*.xml 2>/dev/null); do
    echo "  → 失敗當時的請求與回應（MockHttpServletResponse）看 $f"
  done
  [ -n "$results" ] \
    || echo "  → 沒有測試報告，多半是編譯失敗或 Spring 起不來：往上找第一個 [ERROR]"
  echo "  → ArchitectureTest 失敗：訊息的 because 後面有修法；validation.request / validation.response：不符合 docs/api/openapi.yaml"
  [ $# -gt 0 ] || keep_stage_record FAIL
  return 1
}

# ---- 服務 --------------------------------------------------------------------

app_running() { [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; }

cmd_start() {
  require_docker; load_env; require_db; use_jdk17
  app_running && { echo "服務已在跑（pid $(cat $PID_FILE)），要重新啟動請先 ./dev.sh app-stop"; return; }
  mvn -q -B -ntp -DskipTests package
  mkdir -p "$RUN_DIR"
  # review 頁：把 docs/changes/ 與 db/migration/ 當靜態網頁放在 /try/ 底下（只有從這裡啟動才有，不進 jar）
  nohup "$JAVA_HOME/bin/java" -Duser.timezone=Asia/Taipei \
    -Dspring.web.resources.static-locations=file:docs/changes/,file:db/migration/ '-Dspring.mvc.static-path-pattern=/try/**' \
    -jar "$JAR" > "$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"
  for _ in $(seq 1 60); do
    if curl -sf "http://localhost:$APP_PORT/api/health" >/dev/null; then
      echo "服務已啟動：http://localhost:${APP_PORT}（pid $(cat "$PID_FILE")，log：${LOG_FILE}）"
      local t; for t in docs/changes/*/review.html; do
        [ -f "$t" ] && echo "review 頁：http://localhost:${APP_PORT}/try/${t#docs/changes/}"
      done
      return
    fi
    app_running || { tail -30 "$LOG_FILE"; die "服務啟動失敗"; }
    sleep 1
  done
  tail -30 "$LOG_FILE"; die "60 秒內 health check 沒有通過"
}

cmd_stop() {
  if app_running; then kill "$(cat "$PID_FILE")"; rm -f "$PID_FILE"; echo "服務已停止"; else rm -f "$PID_FILE"; echo "服務沒有在跑"; fi
}

# 重啟：程式與 DB 都換成最新（會清空 shop，資料不保留）
cmd_restart() {
  cmd_stop
  cmd_up
  cmd_start
}

cmd_api() {
  local method=${1:-} path=${2:-} body=${3:-}
  [ -n "$method" ] && [ -n "$path" ] || die "用法: ./dev.sh api <METHOD> <path> [JSON body]"
  case "$path" in /*) ;; *) die "path 要以 / 開頭，例如 /api/admin/products/1" ;; esac
  app_running || die "服務沒有在跑，請先 ./dev.sh app-start"
  local args=(-s -X "$method" -w '\nHTTP %{http_code}\n' -H "X-Operator: ${OPERATOR:-ai-demo}")
  [ -n "$body" ] && args+=(-H 'Content-Type: application/json' -d "$body")
  curl "${args[@]}" "http://localhost:$APP_PORT$path"
}

# ---- 需求流程與規格核准關卡 ---------------------------------------------------------
# state.yaml 只讀最上層的 key（行首、沒有縮排），不需要 yaml 解析器
state_get() { sed -n "s/^$2:[[:space:]]*\([^#]*\).*/\1/p" "$1" | head -1 | sed 's/[[:space:]]*$//'; }

# 進行中（status 不是 done）的 state.yaml，一行一個
active_states() {
  local f
  for f in docs/changes/*/state.yaml; do
    [ -f "$f" ] || continue
    case "$f" in docs/changes/_template/*) continue ;; esac
    [ "$(state_get "$f" status)" = done ] || echo "$f"
  done
}

# 規格 = 測案、OpenAPI、migration。規格核准時記下它們的 hash，之後有變動核准就失效
spec_hash() {
  local x
  for x in docs/test-cases/*.md docs/api/openapi.yaml db/migration/*.sql; do
    [ -f "$x" ] && echo "$x $(git hash-object "$x")"
  done | git hash-object --stdin
}

# 印出 approved / missing / stale
spec_status() {
  local a; a=$(dirname "$1")/spec.approved
  [ -f "$a" ] || { echo missing; return; }
  [ "$(sed -n 's/^spec_hash: *//p' "$a")" = "$(spec_hash)" ] && echo approved || echo stale
}

cmd_in_progress() {
  local f n=0
  for f in $(active_states); do
    n=$((n+1))
    echo "$(state_get "$f" change)（$(state_get "$f" title)）：$(state_get "$f" status)，current $(state_get "$f" current)；規格核准：$(spec_status "$f")  → $f"
    awk '/^handoff:/ { p = 1; print; next } p && /^[^ #]/ { exit } p' "$f" | sed 's/^/  /'
  done
  [ $n = 0 ] && echo "沒有進行中的需求（docs/changes/）"
  [ $n -gt 1 ] && echo "WARNING: 同時有 $n 個需求在進行，一次只做一個，先問使用者要做哪一個"
  return 0
}

cmd_approve() {
  local slug=${1:-} f
  if [ -z "$slug" ]; then
    f=$(active_states)
    [ -n "$f" ] || die "沒有進行中的需求"
    [ "$(echo "$f" | wc -l | tr -d ' ')" = 1 ] || die "有多個進行中的需求，請指定：./dev.sh approve <slug>"
  else
    f=docs/changes/$slug/state.yaml
    [ -f "$f" ] || die "找不到 $f"
  fi
  {
    echo "# 由 ./dev.sh approve 產生，代表使用者核准了規格（測案、OpenAPI、migration）。AI 不能建立或修改這個檔案。"
    echo "approved_at: $(date '+%Y-%m-%d %H:%M')"
    echo "spec_hash: $(spec_hash)"
  } > "$(dirname "$f")/spec.approved"
  echo "已寫入 $(dirname "$f")/spec.approved。回到 Claude Code 告訴 AI「規格通過，繼續」"
}

# PreToolUse hook（scripts/spec-gate.sh）用：有進行中的需求、規格卻沒核准（或已失效）就回 1 並印出原因
cmd_spec_check() {
  local f st rc=0
  for f in $(active_states); do
    st=$(spec_status "$f")
    [ "$st" = approved ] && continue
    echo "$(state_get "$f" change) 的規格還沒核准（${st}）：停下來請使用者 review 測案、OpenAPI、migration；使用者同意後會自己在對話輸入「規格通過」（或執行 ./dev.sh approve）"
    rc=1
  done
  return $rc
}

# 收工檢查：每一輪結束前跑。✗ 要處理掉；「提醒」是給人看的現況，不算沒過
cmd_clean_check() {
  local bad=0 d f n cur upd
  cc_ok()   { echo "  ✓ $*"; }
  cc_fail() { echo "  ✗ $*"; bad=1; }
  cc_note() { echo "  · 提醒：$*"; }
  echo "---- 收工檢查 ----"

  # 1 需求資料夾都有 state.yaml
  n=0
  for d in docs/changes/*/; do
    d=${d%/}
    case "$(basename "$d")" in _*) continue ;; esac
    [ -f "$d/state.yaml" ] || { cc_fail "$d 沒有 state.yaml（空的或沒建完的需求資料夾）"; n=1; }
  done
  [ $n = 0 ] && cc_ok "需求資料夾都有 state.yaml"

  # 2 一次只做一個需求；進行中的需求交接有更新
  n=$(active_states | grep -c . || true)
  if [ "$n" -gt 1 ]; then cc_fail "同時有 $n 個需求在進行，一次只做一個"
  elif [ "$n" = 0 ]; then cc_ok "沒有進行中的需求"
  else
    f=$(active_states); cur=$(state_get "$f" current)
    upd=$(sed -n '/^handoff:/,$p' "$f" | sed -n 's/^  updated:[[:space:]]*"\{0,1\}\([0-9-]*\).*/\1/p' | head -1)
    if [ "$upd" = "$(date +%F)" ]; then cc_ok "$(state_get "$f" change) 停在 ${cur}，handoff 今天更新過"
    else cc_fail "$f 的 handoff.updated 不是今天（${upd:-沒填}）：更新 current 與 handoff 再收工"; fi
  fi

  # 3 最後一次完整檢查對得上現在的檔案
  if [ ! -f "$CHECK_RECORD" ]; then cc_fail "還沒跑過 ./dev.sh check"
  elif [ "$(state_get "$CHECK_RECORD" scope)" != 全部 ]; then cc_fail "最後一次只跑了部分測試：再跑一次 ./dev.sh check（不帶參數）"
  elif [ "$(state_get "$CHECK_RECORD" spec_hash)" != "$(spec_hash)" ] \
    || [ "$(state_get "$CHECK_RECORD" test_hash)" != "$(tree_hash src/test)" ] \
    || [ "$(state_get "$CHECK_RECORD" main_hash)" != "$(tree_hash src/main)" ]; then
    cc_fail "最後一次完整檢查之後，規格、測試或程式又改過：再跑一次 ./dev.sh check"
  elif [ "$(state_get "$CHECK_RECORD" result)" = PASS ]; then cc_ok "最後一次完整檢查是綠燈，之後規格、測試、程式都沒再改"
  else
    case "${cur:-}" in
      5-red|6-green) cc_note "最後一次完整檢查是紅燈（${cur} 階段預期內），handoff.note 要寫清楚紅在哪" ;;
      *) cc_fail "最後一次完整檢查是紅燈：不能留著紅燈收工，先回報" ;;
    esac
  fi

  # 4 功能清單
  if features_registered; then cc_ok "每份測案都登記在 ${FEATURE_LIST}"; else bad=1; fi

  # 5 給人看的現況
  n=$(git status --porcelain | grep -c . || true)
  if [ "$n" = 0 ]; then cc_ok "沒有還沒 commit 的變更（分支 $(git branch --show-current)）"
  else
    cc_note "分支 $(git branch --show-current) 有 $n 個檔還沒 commit（階段做完要 commit；做到一半就在 handoff.note 寫清楚）"
    git status --short | sed 's/^/      /'
  fi
  for f in "$RUN_DIR"/*; do
    [ -e "$f" ] || continue
    case "$(basename "$f")" in app.log|app.pid|check-last.txt) ;; *) cc_note "$f 不是 dev.sh 產生的檔，用不到就刪掉" ;; esac
  done
  app_running && cc_note "服務還在跑（回報後留著給使用者試是正常的）"

  if [ $bad = 0 ]; then echo "CLEAN: PASS"; else echo "CLEAN: FAIL（✗ 的項目處理完再收工）"; fi
  return $bad
}

case "${1:-}" in
  db-up)       shift; cmd_up ;;
  db-down)     shift; cmd_down ;;
  sql)         shift; cmd_sql "$@" ;;
  check)       shift; cmd_check "$@" ;;
  clean-check) shift; cmd_clean_check ;;
  app-start)   shift; cmd_start ;;
  app-stop)    shift; cmd_stop ;;
  app-restart) shift; cmd_restart ;;
  api)         shift; cmd_api "$@" ;;
  in-progress) shift; cmd_in_progress ;;
  approve)     shift; cmd_approve "$@" ;;
  _spec-check) shift; cmd_spec_check ;;
  *) sed -n '2,/^set -euo/p' "$0" | sed '$d' | sed 's/^# \{0,1\}//'; exit 1 ;;
esac
