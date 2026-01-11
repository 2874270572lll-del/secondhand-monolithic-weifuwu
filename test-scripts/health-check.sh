#!/bin/bash

# 快速健康检查 - 测试环境所有服务
# 测试环境: http://localhost:9080/api

API_BASE_URL="http://localhost:9080/api"

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "========================================"
echo "   测试环境服务健康检查"
echo "========================================"
echo ""

check_service() {
    local service_name=$1
    local endpoint=$2
    local expected_code=$3

    echo -n "检查 $service_name ... "

    http_code=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE_URL$endpoint")

    if [ "$http_code" -eq "$expected_code" ]; then
        echo -e "${GREEN}✓ 正常 ($http_code)${NC}"
        return 0
    else
        echo -e "${RED}✗ 异常 (期望:$expected_code, 实际:$http_code)${NC}"
        return 1
    fi
}

TOTAL=0
PASSED=0

# 检查网关
echo -e "${BLUE}=== 基础服务检查 ===${NC}"
check_service "API网关" "/product" "401" && PASSED=$((PASSED+1))
TOTAL=$((TOTAL+1))

# 检查认证服务
echo ""
echo -e "${BLUE}=== 用户服务检查 ===${NC}"
check_service "用户注册接口" "/auth/register" "405" && PASSED=$((PASSED+1))  # POST方法，GET会返回405
TOTAL=$((TOTAL+1))

check_service "用户登录接口" "/auth/login" "405" && PASSED=$((PASSED+1))
TOTAL=$((TOTAL+1))

# 检查商品服务（无需认证）
echo ""
echo -e "${BLUE}=== 商品服务检查 ===${NC}"

# 先注册并登录一个测试用户获取token
TEST_USER="healthcheck_$(date +%s)"
REGISTER_RESP=$(curl -s -X POST "$API_BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$TEST_USER\",\"password\":\"Test123\",\"phone\":\"13800000000\",\"email\":\"test@test.com\"}")

LOGIN_RESP=$(curl -s -X POST "$API_BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$TEST_USER\",\"password\":\"Test123\"}")

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✓ 获取测试Token成功${NC}"

    # 使用token测试商品服务
    http_code=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE_URL/product" \
        -H "Authorization: Bearer $TOKEN")

    echo -n "检查 商品列表接口 ... "
    if [ "$http_code" -eq 200 ]; then
        echo -e "${GREEN}✓ 正常 ($http_code)${NC}"
        PASSED=$((PASSED+1))
    else
        echo -e "${RED}✗ 异常 ($http_code)${NC}"
    fi
    TOTAL=$((TOTAL+1))

    # 检查订单服务
    echo ""
    echo -e "${BLUE}=== 订单服务检查 ===${NC}"
    http_code=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE_URL/order" \
        -H "Authorization: Bearer $TOKEN")

    echo -n "检查 订单列表接口 ... "
    if [ "$http_code" -eq 200 ]; then
        echo -e "${GREEN}✓ 正常 ($http_code)${NC}"
        PASSED=$((PASSED+1))
    else
        echo -e "${RED}✗ 异常 ($http_code)${NC}"
    fi
    TOTAL=$((TOTAL+1))

    # 检查评论服务（无需认证可查看）
    echo ""
    echo -e "${BLUE}=== 评论服务检查 ===${NC}"
    http_code=$(curl -s -o /dev/null -w "%{http_code}" "$API_BASE_URL/comment")

    echo -n "检查 评论列表接口 ... "
    if [ "$http_code" -eq 200 ]; then
        echo -e "${GREEN}✓ 正常 ($http_code)${NC}"
        PASSED=$((PASSED+1))
    else
        echo -e "${RED}✗ 异常 ($http_code)${NC}"
    fi
    TOTAL=$((TOTAL+1))

else
    echo -e "${RED}✗ 无法获取测试Token，跳过需要认证的接口检查${NC}"
fi

# 检查Nacos服务注册
echo ""
echo -e "${BLUE}=== Nacos服务注册检查 ===${NC}"

check_nacos_service() {
    local service_name=$1
    echo -n "检查 $service_name 注册 ... "

    instances=$(curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=$service_name&namespaceId=test")
    count=$(echo "$instances" | grep -o '"count":[0-9]*' | grep -o '[0-9]*')

    if [ -n "$count" ] && [ "$count" -gt 0 ]; then
        echo -e "${GREEN}✓ 已注册 ($count 实例)${NC}"
        return 0
    else
        echo -e "${RED}✗ 未注册${NC}"
        return 1
    fi
}

check_nacos_service "gateway-service" && PASSED=$((PASSED+1))
TOTAL=$((TOTAL+1))

check_nacos_service "user-service" && PASSED=$((PASSED+1))
TOTAL=$((TOTAL+1))

check_nacos_service "product-service" && PASSED=$((PASSED+1))
TOTAL=$((TOTAL+1))

check_nacos_service "order-service" && PASSED=$((PASSED+1))
TOTAL=$((TOTAL+1))

check_nacos_service "comment-service" && PASSED=$((PASSED+1))
TOTAL=$((TOTAL+1))

# RabbitMQ检查
echo ""
echo -e "${BLUE}=== RabbitMQ检查 ===${NC}"
echo -n "检查 RabbitMQ管理API ... "
http_code=$(curl -s -o /dev/null -w "%{http_code}" -u admin:admin123 "http://localhost:15672/api/overview")
if [ "$http_code" -eq 200 ]; then
    echo -e "${GREEN}✓ 正常${NC}"
    PASSED=$((PASSED+1))
else
    echo -e "${RED}✗ 异常${NC}"
fi
TOTAL=$((TOTAL+1))

# 总结
echo ""
echo "========================================"
echo "          检查结果汇总"
echo "========================================"
echo "总检查项: $TOTAL"
echo -e "${GREEN}通过: $PASSED${NC}"
echo -e "${RED}失败: $((TOTAL-PASSED))${NC}"
echo "========================================"

if [ $PASSED -eq $TOTAL ]; then
    echo -e "${GREEN}✓ 所有服务运行正常！${NC}"
    exit 0
else
    echo -e "${RED}✗ 部分服务异常，请检查日志${NC}"
    echo ""
    echo "排查建议："
    echo "1. 检查容器状态: docker ps | grep test"
    echo "2. 查看服务日志: docker logs <container-name>"
    echo "3. 检查Nacos: http://localhost:8848/nacos"
    echo "4. 使用管理脚本: ./manage-env.sh"
    exit 1
fi
