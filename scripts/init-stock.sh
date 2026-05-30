#!/bin/bash
# Initialize Redis stock for product 1 with 100000 units
# Usage: ./scripts/init-stock.sh [redis-host] [redis-port]

REDIS_HOST="${1:-localhost}"
REDIS_PORT="${2:-6379}"

echo "Setting stock for product:1 to 100000..."
redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" SET stock:1 100000

STOCK=$(redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" GET stock:1)
echo "Verified stock:1 = $STOCK"
