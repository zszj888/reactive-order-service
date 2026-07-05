#!/bin/bash
# verify.sh — Health check and smoke test for reactive-order-service
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
PASS=0
FAIL=0

pass() {
    echo "  ✓ $1"
    ((PASS++))
}

fail() {
    echo "  ✗ $1"
    ((FAIL++))
}

echo "=========================================="
echo "  reactive-order-service — Verification"
echo "=========================================="
echo ""

# --- 1. Health check ---
echo "[1] Actuator health check"
HEALTH=$(curl -sf "$BASE_URL/actuator/health" || true)
if echo "$HEALTH" | grep -q '"status":"UP"'; then
    pass "Health endpoint returned UP"
else
    fail "Health endpoint: $HEALTH"
fi

# --- 2. Redis connectivity ---
#echo "[2] Redis check via actuator"
#REDIS=$(curl -sf "$BASE_URL/actuator/health" || true)
#if echo "$REDIS" | grep -q '"redis"'; then
#    pass "Redis is connected"
#else
#    fail "Redis not reported in health"
#fi

# --- 3. DB connectivity ---
echo "[3] Database check via actuator"
DB=$(curl -sf "$BASE_URL/actuator/health" || true)
if echo "$DB" | grep -q '"db"'; then
    pass "Database is connected"
else
    fail "Database not reported in health"
fi

# --- 4. Create order (success) ---
echo "[4] POST /orders — success case"
RESP=$(curl -sf -X POST "$BASE_URL/orders" \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"productId":1,"quantity":1}' || true)
if echo "$RESP" | grep -q '"orderId"'; then
    pass "Order created: $(echo "$RESP" | head -c 80)"
else
    fail "Order creation failed: $RESP"
fi

# --- 5. Create order (sold out) ---
echo "[5] POST /orders — sold out case"
SOLD_OUT=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/orders" \
    -H "Content-Type: application/json" \
    -d '{"userId":2,"productId":999,"quantity":1}' || true)
if [ "$SOLD_OUT" = "409" ]; then
    pass "Sold out returned HTTP 409"
else
    fail "Expected 409, got $SOLD_OUT"
fi

# --- 6. Metrics endpoint ---
echo "[6] Prometheus metrics"
METRICS=$(curl -sf "$BASE_URL/actuator/prometheus" | head -c 200 || true)
if [ -n "$METRICS" ]; then
    pass "Prometheus metrics available"
else
    fail "No prometheus metrics"
fi

echo ""
echo "=========================================="
echo "  Results: $PASS passed, $FAIL failed"
echo "=========================================="

exit $FAIL
