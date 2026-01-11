#!/bin/bash

# 二手交易平台 - 性能测试脚本
# 测试工具: wrk (需要先安装: sudo apt-get install wrk)

echo "=========================================="
echo "二手交易平台性能测试"
echo "测试日期: $(date +%Y-%m-%d\ %H:%M:%S)"
echo "=========================================="
echo ""

# 配置
GATEWAY_URL="http://localhost:8080"
REPORT_DIR="./test-results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# 创建报告目录
mkdir -p ${REPORT_DIR}

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 检查wrk是否安装
if ! command -v wrk &> /dev/null; then
    echo -e "${YELLOW}wrk未安装，正在尝试安装...${NC}"
    echo "请运行: sudo apt-get install wrk"
    echo "或者从源码编译: git clone https://github.com/wg/wrk.git && cd wrk && make"
    exit 1
fi

# 检查服务状态
echo -e "${BLUE}>>> 检查服务状态...${NC}"
if ! curl -s ${GATEWAY_URL}/actuator/health > /dev/null 2>&1; then
    echo -e "${RED}Gateway服务未启动，请先启动服务${NC}"
    exit 1
fi
echo -e "${GREEN}服务正常运行${NC}"
echo ""

# 获取系统信息
echo "=========================================="
echo "测试环境信息"
echo "=========================================="
echo "操作系统: $(uname -s)"
echo "内核版本: $(uname -r)"
echo "CPU信息: $(grep "model name" /proc/cpuinfo | head -1 | cut -d: -f2 | xargs)"
echo "CPU核心数: $(nproc)"
echo "内存总量: $(free -h | grep Mem | awk '{print $2}')"
echo "Docker版本: $(docker --version)"
echo ""

# 测试场景1: 商品列表查询（公开接口，无需认证）
echo "=========================================="
echo "场景1: 商品列表查询压力测试"
echo "=========================================="
echo "测试路径: GET /api/product"
echo "并发用户: 100"
echo "测试时长: 60秒"
echo "线程数: 4"
echo ""

echo -e "${YELLOW}开始测试...${NC}"

wrk -t 4 -c 100 -d 60s --latency ${GATEWAY_URL}/api/product \
  > ${REPORT_DIR}/product-list-test_${TIMESTAMP}.txt 2>&1

echo -e "${GREEN}测试完成${NC}"
echo "结果保存到: ${REPORT_DIR}/product-list-test_${TIMESTAMP}.txt"
echo ""
cat ${REPORT_DIR}/product-list-test_${TIMESTAMP}.txt
echo ""

# 测试场景2: 用户登录接口
echo "=========================================="
echo "场景2: 用户登录压力测试"
echo "=========================================="
echo "测试路径: POST /api/auth/login"
echo "并发用户: 50"
echo "测试时长: 30秒"
echo "线程数: 4"
echo ""

# 创建wrk Lua脚本
cat > /tmp/login-test.lua << 'EOF'
wrk.method = "POST"
wrk.headers["Content-Type"] = "application/json"

request = function()
    local body = string.format('{"username":"testuser","password":"Test123456"}')
    return wrk.format("POST", "/api/auth/login", nil, body)
end
EOF

echo -e "${YELLOW}开始测试...${NC}"

wrk -t 4 -c 50 -d 30s --latency -s /tmp/login-test.lua ${GATEWAY_URL} \
  > ${REPORT_DIR}/login-test_${TIMESTAMP}.txt 2>&1

echo -e "${GREEN}测试完成${NC}"
echo "结果保存到: ${REPORT_DIR}/login-test_${TIMESTAMP}.txt"
echo ""
cat ${REPORT_DIR}/login-test_${TIMESTAMP}.txt
echo ""

# 测试场景3: 混合场景（读多写少）
echo "=========================================="
echo "场景3: 混合场景压力测试（90%读 + 10%写）"
echo "=========================================="
echo "并发用户: 100"
echo "测试时长: 60秒"
echo ""

# 创建混合测试Lua脚本
cat > /tmp/mixed-test.lua << 'EOF'
-- 90%读操作，10%写操作
math.randomseed(os.time())

request = function()
    local rand = math.random(1, 10)

    if rand <= 9 then
        -- 90% 概率：读取商品列表
        return wrk.format("GET", "/api/product")
    else
        -- 10% 概率：创建订单
        wrk.method = "POST"
        wrk.headers["Content-Type"] = "application/json"
        local body = '{"productId":1,"quantity":1,"buyerId":1}'
        return wrk.format("POST", "/api/order", nil, body)
    end
end
EOF

echo -e "${YELLOW}开始测试...${NC}"

wrk -t 4 -c 100 -d 60s --latency -s /tmp/mixed-test.lua ${GATEWAY_URL} \
  > ${REPORT_DIR}/mixed-test_${TIMESTAMP}.txt 2>&1

echo -e "${GREEN}测试完成${NC}"
echo "结果保存到: ${REPORT_DIR}/mixed-test_${TIMESTAMP}.txt"
echo ""
cat ${REPORT_DIR}/mixed-test_${TIMESTAMP}.txt
echo ""

# 测试场景4: 逐步加压测试
echo "=========================================="
echo "场景4: 逐步加压测试"
echo "=========================================="
echo "测试路径: GET /api/product"
echo "并发用户: 10 -> 50 -> 100 -> 200"
echo "每个阶段: 30秒"
echo ""

for concurrency in 10 50 100 200; do
    echo -e "${YELLOW}测试并发数: ${concurrency}${NC}"

    wrk -t 4 -c ${concurrency} -d 30s --latency ${GATEWAY_URL}/api/product \
      > ${REPORT_DIR}/rampup-c${concurrency}_${TIMESTAMP}.txt 2>&1

    echo "结果:"
    grep -E "Requests/sec|Latency" ${REPORT_DIR}/rampup-c${concurrency}_${TIMESTAMP}.txt
    echo ""

    sleep 5  # 间隔5秒
done

# 生成汇总报告
echo "=========================================="
echo "测试汇总"
echo "=========================================="
echo ""
echo "所有测试报告已保存到: ${REPORT_DIR}"
echo ""
echo "文件列表:"
ls -lh ${REPORT_DIR}/*${TIMESTAMP}*
echo ""

# 提取关键指标
echo "关键性能指标汇总:"
echo ""

for file in ${REPORT_DIR}/*${TIMESTAMP}*.txt; do
    filename=$(basename $file)
    echo "--- $filename ---"
    grep -A 1 "Latency" $file | head -2
    grep "Requests/sec" $file
    echo ""
done

echo -e "${GREEN}所有性能测试完成！${NC}"
