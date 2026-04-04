#!/usr/bin/env bash
set -euo pipefail

cd frontend

python -m http.server 5500 --bind 0.0.0.0