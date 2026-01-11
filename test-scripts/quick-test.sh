#!/bin/bash

# 快速功能测试 - 验证所有服务是否正常工作

GATEWAY_URL="http://localhost:8080"
NACOS_URL="http://localhost:8848"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "=========================================="
echo "快速健康检查"
echo "=========================================="
echo ""

# 1. 检查Nacos
echo -n "检查 Nacos... "
if curl -s ${NACOS_URL}/nacos/ > /dev/null; then
    echo -e "${GREEN}✓ 正常${NC}"
else
    echo -e "${RED}✗ 异常${NC}"
fi

# 2. 检查Gateway
echo -n "检查 Gateway... "
HEALTH=$(curl -s ${GATEWAY_URL}/actuator/health 2>/dev/null || echo "error")
if echo "$HEALTH" | grep -q "error"; then
    echo -e "${RED}✗ 异常${NC}"
else
    echo -e "${GREEN}✓ 正常${NC}"
fi

# 3. 检查Gateway路由
echo -n "检查 Gateway路由... "
ROUTES=$(curl -s ${GATEWAY_URL}/actuator/gateway/routes 2>/dev/null || echo "error")
if echo "$ROUTES" | grep -q "error"; then
    echo -e "${RED}✗ 异常${NC}"
else
    ROUTE_COUNT=$(echo "$ROUTES" | grep -o '"route_id"' | wc -l)
    echo -e "${GREEN}✓ 正常 (${ROUTE_COUNT}条路由)${NC}"
fi

# 4. 检查各个服务
echo ""
echo "检查服务注册状态:"

for service in user-service product-service order-service comment-service; do
    echo -n "  $service... "
    INSTANCES=$(curl -s "${NACOS_URL}/nacos/v1/ns/instance/list?serviceName=${service}&namespaceId=dev" 2>/dev/null)

    if echo "$INSTANCES" | grep -q '"hosts":\['; then
        COUNT=$(echo "$INSTANCES" | grep -o '"ip"' | wc -l)
        echo -e "${GREEN}✓ ${COUNT}个实例${NC}"
    else
        echo -e "${RED}✗ 未注册${NC}"
    fi
done

# 5. 测试API端点
echo ""
echo "测试API端点:"

# 测试商品列表
echo -n "  GET /api/product... "
RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null ${GATEWAY_URL}/api/product)
if [ "$RESPONSE" == "200" ]; then
    echo -e "${GREEN}✓ HTTP $RESPONSE${NC}"
else
    echo -e "${RED}✗ HTTP $RESPONSE${NC}"
fi

# 测试商品详情
echo -n "  GET /api/product/1... "
RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null ${GATEWAY_URL}/api/product/1)
if [ "$RESPONSE" == "200" ]; then
    echo -e "${GREEN}✓ HTTP $RESPONSE${NC}"
else
    echo -e "${YELLOW}⚠ HTTP $RESPONSE${NC}"
fi

# 测试评论列表
echo -n "  GET /api/comment/product/1... "
RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null ${GATEWAY_URL}/api/comment/product/1)
if [ "$RESPONSE" == "200" ]; then
    echo -e "${GREEN}✓ HTTP $RESPONSE${NC}"
else
    echo -e "${YELLOW}⚠ HTTP $RESPONSE${NC}"
fi

# 测试未认证的请求（应该返回401）
echo -n "  GET /api/order (未认证)... "
RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null ${GATEWAY_URL}/api/order)
if [ "$RESPONSE" == "401" ]; then
    echo -e "${GREEN}✓ HTTP $RESPONSE (JWT验证生效)${NC}"
else
    echo -e "${YELLOW}⚠ HTTP $RESPONSE (预期401)${NC}"
fi

echo ""
echo "=========================================="
echo "快速检查完成"
echo "=========================================="
