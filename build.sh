#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$ROOT_DIR/out"
VERSION="${1:-SNAPSHOT}"
JAR_NAME="locolang-${VERSION}.jar"

mkdir -p "$OUT_DIR"
javac -d "$OUT_DIR" "$ROOT_DIR"/src/analyze/*.java
if [ -f "$ROOT_DIR/resource/std.ll" ]; then
  cp "$ROOT_DIR/resource/std.ll" "$OUT_DIR/std.ll"
else
  echo "Warning: $ROOT_DIR/resource/std.ll not found; std module won't be bundled."
fi
jar --create --file "$ROOT_DIR/$JAR_NAME" --main-class analyze.ExpressionAnalyzer -C "$OUT_DIR" .
echo "Built $ROOT_DIR/$JAR_NAME"
