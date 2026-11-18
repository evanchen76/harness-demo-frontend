#!/usr/bin/env bash
# Claude Code PreToolUse hook（Edit / Write / MultiEdit / NotebookEdit）：規格核准關卡。
# 有進行中的需求時，規格沒核准（沒有 spec.approved，或核准後規格又變了），
# 就不讓 AI 改 src/（測試與實作）。exit 2 擋下這次工具呼叫，stderr 的訊息會回給 AI。
# 沒有進行中的需求時不擋：這個關卡只管「需求流程裡不能跳過規格核准」。
cd "$(dirname "$0")/.."
root=$(pwd -P)

file=$(jq -r '.tool_input.file_path // .tool_input.notebook_path // empty' 2>/dev/null)
[ -n "$file" ] || exit 0
case "$file" in /*) ;; *) file="$root/$file" ;; esac
rel=${file#"$root"/}

case "$rel" in
  src/*) ;;
  *) exit 0 ;;   # 規格（docs/、db/migration/）在規格核准之前本來就要寫
esac

if ! reason=$(./dev.sh _spec-check); then
  {
    echo "規格核准關卡：不能修改 $rel"
    echo "$reason"
  } >&2
  exit 2
fi
exit 0
