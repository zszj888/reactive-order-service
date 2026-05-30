#!/bin/bash
# run-benchmark.sh — Run k6 benchmark
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"

echo "=========================================="
echo "  Running k6 benchmark"
echo "  Target: $BASE_URL"
echo "=========================================="

k6 run benchmark/k6.js \
    -e BASE_URL="$BASE_URL" \
    --summary-trend-stats="avg,min,med,max,p(90),p(95),p(99)"
