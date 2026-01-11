#!/bin/bash

# 二手交易平台 - 功能测试脚本
# 测试日期: $(date +%Y-%m-%d)

echo "=========================================="
echo "二手交易平台功能测试"
echo "=========================================="
echo ""

# 配置
GATEWAY_URL="http://localhost:8080"
TEST_USER="testuser_$(date +%s)"
TEST_PASSWORD="Test123456"
TOKEN=""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL=0
PASSED=0
FAILED=0

# 测试结果函数
test_result() {
    local test_id=$1
    local test_name=$2
    local expected=$3
    local actual=$4

    TOTAL=$((TOTAL + 1))

    if [ "$expected" == "$actual" ]; then
        echo -e "${GREEN}[PASS]${NC} $test_id - $test_name"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}[FAIL]${NC} $test_id - $test_name"
        echo -e "  预期: $expected"
        echo -e "  实际: $actual"
        FAILED=$((FAILED + 1))
        return 1
    fi
}

# 等待服务启动
echo -e "${YELLOW}>>> 检查服务状态...${NC}"
for i in {1..30}; do
    if curl -s ${GATEWAY_URL}/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}Gateway服务已就绪${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}Gateway服务未启动，测试终止${NC}"
        exit 1
    fi
    echo "等待服务启动... ($i/30)"
    sleep 2
done

echo ""
echo "=========================================="
echo "TC001 - 用户注册测试"
echo "=========================================="

# 注册新用户
REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST ${GATEWAY_URL}/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"${TEST_USER}\",
    \"password\": \"${TEST_PASSWORD}\",
    \"email\": \"${TEST_USER}@test.com\"
  }")

HTTP_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$REGISTER_RESPONSE" | sed '$d')

echo "请求: POST ${GATEWAY_URL}/api/auth/register"
echo "用户名: ${TEST_USER}"
echo "响应: ${RESPONSE_BODY}"
echo "状态码: ${HTTP_CODE}"

if [ "$HTTP_CODE" == "201" ] || [ "$HTTP_CODE" == "200" ]; then
    test_result "TC001" "用户注册" "成功" "成功"
else
    test_result "TC001" "用户注册" "成功" "失败(HTTP ${HTTP_CODE})"
fi

echo ""
echo "=========================================="
echo "TC002 - 用户登录测试"
echo "=========================================="

# 用户登录
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST ${GATEWAY_URL}/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"${TEST_USER}\",
    \"password\": \"${TEST_PASSWORD}\"
  }")

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

echo "请求: POST ${GATEWAY_URL}/api/auth/login"
echo "响应: ${RESPONSE_BODY}"
echo "状态码: ${HTTP_CODE}"

# 提取token
TOKEN=$(echo "$RESPONSE_BODY" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ "$HTTP_CODE" == "200" ] && [ ! -z "$TOKEN" ]; then
    test_result "TC002" "用户登录" "成功" "成功"
    echo "Token: ${TOKEN:0:50}..."
else
    test_result "TC002" "用户登录" "成功" "失败(HTTP ${HTTP_CODE})"
fi

echo ""
echo "=========================================="
echo "TC003 - 服务发现测试"
echo "=========================================="

# 查询Nacos服务实例
SERVICES_RESPONSE=$(curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=product-service&namespaceId=dev")

echo "请求: GET Nacos服务实例列表"
echo "响应: ${SERVICES_RESPONSE}"

# 检查是否有服务实例
INSTANCE_COUNT=$(echo "$SERVICES_RESPONSE" | grep -o '"hosts":\[' | wc -l)

if [ $INSTANCE_COUNT -gt 0 ]; then
    test_result "TC003" "服务发现" "发现服务实例" "发现服务实例"
else
    test_result "TC003" "服务发现" "发现服务实例" "未发现服务实例"
fi

echo ""
echo "=========================================="
echo "TC004 - 负载均衡测试"
echo "=========================================="

# 从Nacos查询product-service实例数
NACOS_INSTANCES=$(curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=product-service&namespaceId=dev")
INSTANCE_COUNT=$(echo "$NACOS_INSTANCES" | grep -o '"ip":"[^"]*"' | wc -l)

echo "从Nacos查询到 product-service 实例数: $INSTANCE_COUNT"

# 发送20次请求，观察响应头中的实例标识
echo "发送20次请求验证负载均衡..."
echo ""
echo "请求次数 | 实例标识 | 实例IP"
echo "---------|----------|-------------------"

declare -A instance_ips
declare -A instance_counts

for i in {1..20}; do
    # 获取响应头
    HEADERS=$(curl -s -i ${GATEWAY_URL}/api/product -H "Content-Type: application/json" | grep -i "^X-Instance")

    # 提取实例ID和IP
    INSTANCE_ID=$(echo "$HEADERS" | grep -i "X-Instance-Id:" | cut -d: -f2 | tr -d ' \r')
    INSTANCE_IP=$(echo "$HEADERS" | grep -i "X-Instance-IP:" | cut -d: -f2 | tr -d ' \r')

    # 统计每个实例被访问的次数
    if [ ! -z "$INSTANCE_IP" ]; then
        instance_ips[$INSTANCE_IP]=1
        instance_counts[$INSTANCE_IP]=$((${instance_counts[$INSTANCE_IP]:-0} + 1))
        printf "  #%-7d | %-8s | %s\n" $i "$INSTANCE_ID" "$INSTANCE_IP"
    else
        printf "  #%-7d | %-8s | %s\n" $i "未获取" "未获取"
    fi

    sleep 0.2
done

echo ""
UNIQUE_INSTANCES=${#instance_ips[@]}
echo "访问了 $UNIQUE_INSTANCES 个不同的实例"

# 显示每个实例的访问次数
if [ $UNIQUE_INSTANCES -gt 0 ]; then
    echo ""
    echo "各实例访问次数统计："
    for ip in "${!instance_counts[@]}"; do
        echo "  $ip: ${instance_counts[$ip]} 次"
    done
fi

echo ""

# 验证负载均衡是否工作
if [ $INSTANCE_COUNT -lt 2 ]; then
    test_result "TC004" "负载均衡" "有多个实例" "仅有${INSTANCE_COUNT}个实例"
elif [ $UNIQUE_INSTANCES -lt 2 ]; then
    test_result "TC004" "负载均衡" "访问多个实例" "仅访问了${UNIQUE_INSTANCES}个实例"
else
    test_result "TC004" "负载均衡" "访问多个实例" "访问多个实例"
fi

echo ""
echo "=========================================="
echo "测试总结"
echo "=========================================="
echo "总测试用例: $TOTAL"
echo -e "${GREEN}通过: $PASSED${NC}"
echo -e "${RED}失败: $FAILED${NC}"

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}部分测试失败${NC}"
    exit 1
fi
