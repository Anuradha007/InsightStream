#!/usr/bin/env bash
# Start Chroma DB server (port 8000). Run from repo root. Requires .venv-chroma with chromadb installed.
set -e
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"
CHROMA_DATA="${CHROMA_DATA:-$REPO_ROOT/chroma_data}"
if [[ ! -d .venv-chroma ]]; then
  echo "Missing .venv-chroma. Create it and install chromadb first:"
  echo "  python3 -m venv .venv-chroma && .venv-chroma/bin/pip install chromadb"
  exit 1
fi
echo "Starting Chroma at http://localhost:8000 (data: $CHROMA_DATA)"
exec "$REPO_ROOT/.venv-chroma/bin/chroma" run --path "$CHROMA_DATA" --port 8000
