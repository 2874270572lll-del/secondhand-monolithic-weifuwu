#!/bin/bash

# 二手交易平台集成测试脚本（测试环境）
# 重写版 - 增强调试和错误处理

API_BASE="http://localhost:9080/api"
RABBITMQ_MGMT="http://localhost:15672/api"
RABBITMQ_USER="admin"
RABBITMQ_PASS="admin123"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

echo "========================================"
echo " 二手交易平台集成测试 - 测试环境"
echo "========================================"
echo ""

# 简化的测试函数
run_test() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local token=$5

    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    echo -e "${YELLOW}[测试 $TOTAL_TESTS] $name${NC}" >&2

    # 构建curl命令
    if [ -n "$token" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -w "\n%{http_code}" -X $method "$API_BASE$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $token" \
                -d "$data")
        else
            response=$(curl -s -w "\n%{http_code}" -X $method "$API_BASE$url" \
                -H "Authorization: Bearer $token")
        fi
    else
        if [ -n "$data" ]; then
            response=$(curl -s -w "\n%{http_code}" -X $method "$API_BASE$url" \
                -H "Content-Type: application/json" \
                -d "$data")
        else
            response=$(curl -s -w "\n%{http_code}" -X $method "$API_BASE$url")
        fi
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    echo "请求: $method $url" >&2
    echo "状态码: $http_code" >&2
    echo "响应: $body" >&2

    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✓ 通过${NC}" >&2
        PASSED_TESTS=$((PASSED_TESTS + 1))
        # 只输出JSON到stdout
        echo "$body"
        return 0
    else
        echo -e "${RED}✗ 失败${NC}" >&2
        FAILED_TESTS=$((FAILED_TESTS + 1))
        # 失败时也输出body，方便调试
        echo "$body"
        return 1
    fi
}

# ========================================
# 测试开始
# ========================================

TIMESTAMP=$(date +%s)
SELLER_USERNAME="seller_$TIMESTAMP"
SELLER_PASSWORD="Seller123456"
BUYER_USERNAME="buyer_$TIMESTAMP"
BUYER_PASSWORD="Buyer123456"

echo -e "${BLUE}========== 步骤1: 注册卖家用户 ==========${NC}"
echo ""

SELLER_RESPONSE=$(run_test "注册卖家" "POST" "/auth/register" '{"username":"'$SELLER_USERNAME'","password":"'$SELLER_PASSWORD'","phone":"13800001111","email":"seller_'$TIMESTAMP'@example.com"}')

if [ $? -eq 0 ]; then
    # 检查业务逻辑code
    if command -v jq &> /dev/null; then
        RESPONSE_CODE=$(echo "$SELLER_RESPONSE" | jq -r '.code // empty')
    else
        RESPONSE_CODE=$(echo "$SELLER_RESPONSE" | grep -o '"code":[0-9]*' | head -1 | grep -o '[0-9]*')
    fi

    if [ "$RESPONSE_CODE" != "200" ]; then
        echo -e "${RED}注册失败: $(echo "$SELLER_RESPONSE" | grep -o '"message":"[^"]*"' | cut -d'"' -f4)${NC}"
        exit 1
    fi

    # 提取ID - 使用jq如果可用，否则用grep
    if command -v jq &> /dev/null; then
        SELLER_ID=$(echo "$SELLER_RESPONSE" | jq -r '.data.id // empty')
    else
        SELLER_ID=$(echo "$SELLER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
    fi

    if [ -z "$SELLER_ID" ]; then
        echo -e "${RED}无法提取卖家ID，响应可能格式不正确${NC}"
        echo "完整响应: $SELLER_RESPONSE"
        exit 1
    fi

    echo -e "${GREEN}卖家ID: $SELLER_ID${NC}"
else
    echo -e "${RED}注册卖家失败${NC}"
    exit 1
fi
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤2: 注册买家用户 ==========${NC}"
echo ""

BUYER_RESPONSE=$(run_test "注册买家" "POST" "/auth/register" '{"username":"'$BUYER_USERNAME'","password":"'$BUYER_PASSWORD'","phone":"13800002222","email":"buyer_'$TIMESTAMP'@example.com"}')

if [ $? -eq 0 ]; then
    # 检查业务逻辑code
    if command -v jq &> /dev/null; then
        RESPONSE_CODE=$(echo "$BUYER_RESPONSE" | jq -r '.code // empty')
    else
        RESPONSE_CODE=$(echo "$BUYER_RESPONSE" | grep -o '"code":[0-9]*' | head -1 | grep -o '[0-9]*')
    fi

    if [ "$RESPONSE_CODE" != "200" ]; then
        echo -e "${RED}注册失败: $(echo "$BUYER_RESPONSE" | grep -o '"message":"[^"]*"' | cut -d'"' -f4)${NC}"
        exit 1
    fi

    if command -v jq &> /dev/null; then
        BUYER_ID=$(echo "$BUYER_RESPONSE" | jq -r '.data.id // empty')
    else
        BUYER_ID=$(echo "$BUYER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
    fi

    if [ -z "$BUYER_ID" ]; then
        echo -e "${RED}无法提取买家ID${NC}"
        echo "完整响应: $BUYER_RESPONSE"
        exit 1
    fi

    echo -e "${GREEN}买家ID: $BUYER_ID${NC}"
else
    echo -e "${RED}注册买家失败${NC}"
    exit 1
fi
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤3: 卖家登录 ==========${NC}"
echo ""

SELLER_LOGIN_RESPONSE=$(run_test "卖家登录" "POST" "/auth/login" '{"username":"'$SELLER_USERNAME'","password":"'$SELLER_PASSWORD'"}')

if [ $? -eq 0 ]; then
    if command -v jq &> /dev/null; then
        SELLER_TOKEN=$(echo "$SELLER_LOGIN_RESPONSE" | jq -r '.data.token // empty')
    else
        SELLER_TOKEN=$(echo "$SELLER_LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    fi

    if [ -z "$SELLER_TOKEN" ]; then
        echo -e "${RED}无法提取卖家Token${NC}"
        echo "完整响应: $SELLER_LOGIN_RESPONSE"
        exit 1
    fi

    echo -e "${GREEN}卖家Token: ${SELLER_TOKEN:0:30}...${NC}"
else
    echo -e "${RED}卖家登录失败${NC}"
    exit 1
fi
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤4: 卖家发布商品 ==========${NC}"
echo ""

PRODUCT_RESPONSE=$(run_test "发布商品" "POST" "/product" '{"name":"测试商品-二手iPhone14","description":"99新iPhone14，仅使用3个月，无磕碰无划痕","price":4999.00,"stock":1,"category":"数码产品","sellerId":'$SELLER_ID',"status":1}' "$SELLER_TOKEN")

if [ $? -eq 0 ]; then
    if command -v jq &> /dev/null; then
        PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | jq -r '.data.id // empty')
    else
        PRODUCT_ID=$(echo "$PRODUCT_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
    fi

    if [ -z "$PRODUCT_ID" ]; then
        echo -e "${RED}无法提取商品ID${NC}"
        echo "完整响应: $PRODUCT_RESPONSE"
        exit 1
    fi

    echo -e "${GREEN}商品ID: $PRODUCT_ID${NC}"
else
    echo -e "${RED}发布商品失败${NC}"
    exit 1
fi
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤5: 买家登录 ==========${NC}"
echo ""

BUYER_LOGIN_RESPONSE=$(run_test "买家登录" "POST" "/auth/login" '{"username":"'$BUYER_USERNAME'","password":"'$BUYER_PASSWORD'"}')

if [ $? -eq 0 ]; then
    if command -v jq &> /dev/null; then
        BUYER_TOKEN=$(echo "$BUYER_LOGIN_RESPONSE" | jq -r '.data.token // empty')
    else
        BUYER_TOKEN=$(echo "$BUYER_LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    fi

    if [ -z "$BUYER_TOKEN" ]; then
        echo -e "${RED}无法提取买家Token${NC}"
        echo "完整响应: $BUYER_LOGIN_RESPONSE"
        exit 1
    fi

    echo -e "${GREEN}买家Token: ${BUYER_TOKEN:0:30}...${NC}"
else
    echo -e "${RED}买家登录失败${NC}"
    exit 1
fi
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤6: 买家浏览商品 ==========${NC}"
echo ""

run_test "查看所有商品" "GET" "/product" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

run_test "查看商品详情" "GET" "/product/$PRODUCT_ID" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

run_test "按类别查看商品" "GET" "/product/category/数码产品" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤7: 买家创建订单 ==========${NC}"
echo ""

ORDER_RESPONSE=$(run_test "创建订单" "POST" "/order" '{"productId":'$PRODUCT_ID',"userId":'$BUYER_ID',"quantity":1,"totalPrice":4999.00,"shippingAddress":"浙江省杭州市西湖区某某街道123号"}' "$BUYER_TOKEN")

if [ $? -eq 0 ]; then
    if command -v jq &> /dev/null; then
        ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.data.id // empty')
        ORDER_NO=$(echo "$ORDER_RESPONSE" | jq -r '.data.orderNo // empty')
    else
        ORDER_ID=$(echo "$ORDER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
        ORDER_NO=$(echo "$ORDER_RESPONSE" | grep -o '"orderNo":"[^"]*"' | cut -d'"' -f4)
    fi

    if [ -z "$ORDER_ID" ]; then
        echo -e "${RED}无法提取订单ID${NC}"
        echo "完整响应: $ORDER_RESPONSE"
        exit 1
    fi

    echo -e "${GREEN}订单ID: $ORDER_ID${NC}"
    echo -e "${GREEN}订单号: $ORDER_NO${NC}"
else
    echo -e "${RED}创建订单失败${NC}"
    exit 1
fi
echo ""
sleep 2

# ========================================
echo -e "${BLUE}========== 步骤8: 查看RabbitMQ消息队列 ==========${NC}"
echo ""

echo "查询订单队列消息数量..."
ORDER_QUEUE=$(curl -s -u $RABBITMQ_USER:$RABBITMQ_PASS "$RABBITMQ_MGMT/queues/%2F/order.created.queue" 2>/dev/null)
if [ $? -eq 0 ]; then
    MESSAGE_COUNT=$(echo "$ORDER_QUEUE" | grep -o '"messages":[0-9]*' | head -1 | grep -o '[0-9]*')
    echo -e "${GREEN}订单队列消息数: ${MESSAGE_COUNT:-0}${NC}"
else
    echo -e "${YELLOW}⚠ 无法连接RabbitMQ管理接口${NC}"
fi
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤9: 查看订单详情 ==========${NC}"
echo ""

run_test "根据ID查看订单" "GET" "/order/$ORDER_ID" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

run_test "根据订单号查看订单" "GET" "/order/orderNo/$ORDER_NO" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

run_test "查看买家的所有订单" "GET" "/order/buyer/$BUYER_ID" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤10: 订单支付 ==========${NC}"
echo ""

run_test "支付订单" "PUT" "/order/$ORDER_ID/pay" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤11: 卖家查看订单并发货 ==========${NC}"
echo ""

run_test "卖家查看自己的订单" "GET" "/order/seller/$SELLER_ID" "" "$SELLER_TOKEN" > /dev/null
echo ""
sleep 1

run_test "卖家发货" "PUT" "/order/$ORDER_ID/ship" "" "$SELLER_TOKEN" > /dev/null
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤12: 买家确认收货 ==========${NC}"
echo ""

run_test "买家确认收货" "PUT" "/order/$ORDER_ID/finish" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤13: 买家发布评论 ==========${NC}"
echo ""

COMMENT_RESPONSE=$(run_test "发布商品评论" "POST" "/comment" '{"productId":'$PRODUCT_ID',"userId":'$BUYER_ID',"orderId":'$ORDER_ID',"rating":5,"content":"非常好的商品，卖家服务态度也很好，物流很快！"}' "$BUYER_TOKEN")

if [ $? -eq 0 ]; then
    if command -v jq &> /dev/null; then
        COMMENT_ID=$(echo "$COMMENT_RESPONSE" | jq -r '.data.id // empty')
    else
        COMMENT_ID=$(echo "$COMMENT_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
    fi

    if [ -z "$COMMENT_ID" ]; then
        COMMENT_ID="提取失败"
    fi

    echo -e "${GREEN}评论ID: $COMMENT_ID${NC}"
else
    echo -e "${YELLOW}⚠ 发布评论失败${NC}"
    COMMENT_ID="N/A"
fi
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤14: 查看商品评论 ==========${NC}"
echo ""

run_test "查看商品的所有评论" "GET" "/comment/product/$PRODUCT_ID" > /dev/null
echo ""
sleep 1

run_test "查看买家的所有评论" "GET" "/comment/user/$BUYER_ID" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

run_test "查看订单的评论" "GET" "/comment/order/$ORDER_ID" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤15: 测试订单取消功能 ==========${NC}"
echo ""

NEW_ORDER_RESPONSE=$(run_test "创建新订单用于测试取消" "POST" "/order" '{"productId":'$PRODUCT_ID',"userId":'$BUYER_ID',"quantity":1,"totalPrice":4999.00,"shippingAddress":"浙江省杭州市西湖区某某街道456号"}' "$BUYER_TOKEN")

if [ $? -eq 0 ]; then
    if command -v jq &> /dev/null; then
        NEW_ORDER_ID=$(echo "$NEW_ORDER_RESPONSE" | jq -r '.data.id // empty')
    else
        NEW_ORDER_ID=$(echo "$NEW_ORDER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
    fi

    if [ -n "$NEW_ORDER_ID" ]; then
        echo -e "${GREEN}新订单ID: $NEW_ORDER_ID${NC}"
        echo ""
        sleep 1

        run_test "取消订单" "DELETE" "/order/$NEW_ORDER_ID" "" "$BUYER_TOKEN" > /dev/null
    else
        echo -e "${YELLOW}⚠ 无法提取新订单ID${NC}"
        NEW_ORDER_ID="N/A"
    fi
else
    echo -e "${YELLOW}⚠ 创建测试订单失败${NC}"
    NEW_ORDER_ID="N/A"
fi
echo ""
sleep 1

# ========================================
echo -e "${BLUE}========== 步骤16: 查看最终状态 ==========${NC}"
echo ""

run_test "查看所有订单" "GET" "/order" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

run_test "查看已完成订单" "GET" "/order/status/3" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

run_test "查看所有评论" "GET" "/comment" "" "$BUYER_TOKEN" > /dev/null
echo ""
sleep 1

# ========================================
# 测试总结
# ========================================
echo "========================================"
echo "           测试总结"
echo "========================================"
echo ""
echo "业务流程："
echo "1. 注册卖家: $SELLER_USERNAME (ID: $SELLER_ID)"
echo "2. 注册买家: $BUYER_USERNAME (ID: $BUYER_ID)"
echo "3. 发布商品: ID=$PRODUCT_ID"
echo "4. 创建订单: ID=$ORDER_ID, 订单号=$ORDER_NO"
echo "5. 支付订单 → 发货 → 确认收货"
echo "6. 发布评论: ID=$COMMENT_ID"
echo "7. 取消订单测试: ID=$NEW_ORDER_ID"
echo ""
echo "测试结果："
echo "总测试数: $TOTAL_TESTS"
echo -e "${GREEN}通过: $PASSED_TESTS${NC}"
echo -e "${RED}失败: $FAILED_TESTS${NC}"
echo "========================================"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ 所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}✗ 部分测试失败，请检查日志${NC}"
    exit 1
fi
