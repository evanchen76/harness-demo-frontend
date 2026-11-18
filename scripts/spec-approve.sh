#!/usr/bin/env bash
# Claude Code UserPromptSubmit hook：使用者在對話輸入「規格通過」就核准規格。
# 這個 hook 只在使用者送出訊息時觸發，AI 的工具呼叫觸發不了，所以仍然只有人能核准。
# 整則訊息只有核准語才算（「規格通過了嗎？」、長訊息裡提到規格通過都不算）。
# 只核准 status 是 waiting_review 的需求：AI 還沒做到規格核准（4-approve）就不會被提前核准。
# stdout 會加進這一輪的 context，讓 AI 知道核准結果。
cd "$(dirname "$0")/.." || exit 0

prompt=$(jq -r '.prompt // empty' 2>/dev/null)
said=$(printf '%s' "$prompt" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' | tr 'a-z' 'A-Z')
case "$said" in
  "規格通過"|"規格通過，繼續"|"SPEC APPROVE"|"SPEC APPROVED") ;;
  *) exit 0 ;;
esac

waiting=()
for f in docs/changes/*/state.yaml; do
  [ -f "$f" ] || continue
  case "$f" in docs/changes/_template/*) continue ;; esac
  [ "$(sed -n 's/^status: *//p' "$f" | head -1)" = waiting_review ] && waiting+=("$(basename "$(dirname "$f")")")
done

echo "[規格核准 hook] 使用者在對話輸入了規格核准語。"
case ${#waiting[@]} in
  0) echo "沒有 status 是 waiting_review 的需求，沒有核准任何東西。告訴使用者目前沒有等待規格核准的需求。" ;;
  1) if out=$(./dev.sh approve "${waiting[0]}" 2>&1); then
       echo "已核准 ${waiting[0]}：寫入 docs/changes/${waiting[0]}/spec.approved。照 docs/guide/workflow.md，status 改回 in_progress、current 改 5-red，接著做。"
     else
       echo "核准 ${waiting[0]} 失敗，沒有核准。把這個錯誤回報給使用者：$out"
     fi ;;
  *) echo "有多個需求在等規格核准（${waiting[*]}），沒有核准任何東西。請使用者在終端機執行 ./dev.sh approve <slug> 指定要核准哪一個。" ;;
esac
exit 0
