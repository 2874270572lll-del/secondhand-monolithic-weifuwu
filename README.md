# 二手交易平台 - 微服务架构（阶段3）

## 项目概述

本项目是一个基于Spring Cloud Alibaba的二手交易平台微服务系统，完成了阶段3的服务间通信与负载均衡功能实现。

## 技术栈

- **Java**: 21
- **Spring Boot**: 3.3.6
- **Spring Cloud**: 2023.0.3
- **Spring Cloud Alibaba**: 2023.0.1.2
- **Nacos**: 2.2.3（服务注册与发现、配置中心）
- **OpenFeign**: 服务间调用
- **Spring Cloud LoadBalancer**: 负载均衡
- **Sentinel**: 熔断降级
- **MySQL**: 8.4.0（数据库）
- **JPA/Hibernate**: 持久层框架

## 项目架构

### 微服务模块

项目包含以下4个独立微服务：

1. **user-service（用户服务）** - 端口: 8081
   - 用户注册、登录
   - 用户信息管理
   - 提供用户信息查询接口供其他服务调用

2. **product-service（商品服务）** - 端口: 8082
   - 商品发布、编辑、删除
   - 商品搜索、分类浏览
   - 库存管理
   - 提供商品信息查询和库存减少接口供订单服务调用

3. **order-service（订单服务）** - 端口: 8083
   - 订单创建、支付
   - 订单状态管理（待支付、已支付、已发货、已完成、已取消）
   - 调用商品服务获取商品信息和减少库存
   - 调用用户服务验证用户信息

4. **comment-service（评论服务）** - 端口: 8084
   - 评论发布、查询
   - 评分管理
   - 调用订单服务验证订单信息
   - 调用用户服务获取用户信息

### 服务间调用关系

```
┌──────────────┐
│ user-service │ (被调用)
└──────────────┘
       ↑
       │ UserClient (Feign)
       │
┌──────────────┬──────────────┬────────────────┐
│order-service │comment-service│product-service│
└──────────────┴──────────────┴────────────────┘
       │           ↑                    ↑
       │           │                    │
       │      OrderClient          ProductClient
       │      (Feign)               (Feign)
       │           │                    │
       └───────────┴────────────────────┘
```

## 阶段3核心功能

### 1. OpenFeign服务间调用

所有服务已集成OpenFeign，实现声明式REST客户端调用：

- **ProductClient**: 调用商品服务
  - `getProductById()`: 获取商品详情
  - `reduceStock()`: 减少商品库存

- **UserClient**: 调用用户服务
  - `getUserById()`: 获取用户信息

- **OrderClient**: 调用订单服务
  - `getOrderById()`: 获取订单详情

### 2. 负载均衡

使用Spring Cloud LoadBalancer实现客户端负载均衡：

- 默认策略：轮询（Round Robin）
- 支持多实例部署
- 自动从Nacos获取服务实例列表
- 健康检查与自动剔除故障节点

### 3. 熔断降级

集成Sentinel实现服务容错：

- **熔断保护**: 当服务调用失败率达到阈值时自动熔断
- **降级处理**: 每个Feign客户端都配置了Fallback降级逻辑
- **限流控制**: 支持QPS限流
- **实时监控**: Sentinel Dashboard可视化监控

#### Fallback降级示例

- ProductClientFallback: 商品服务不可用时返回友好错误信息
- UserClientFallback: 用户服务不可用时返回友好错误信息
- OrderClientFallback: 订单服务不可用时返回友好错误信息

### 4. 配置说明

每个服务的`application.yml`都配置了：

```yaml
spring:
  cloud:
    # OpenFeign配置
    openfeign:
      client:
        config:
          default:
            connect-timeout: 10000  # 连接超时时间
            read-timeout: 20000     # 读取超时时间
      sentinel:
        enabled: true               # 启用Sentinel支持

    # Sentinel配置
    sentinel:
      transport:
        dashboard: localhost:8080   # Sentinel控制台地址
        port: 871x                  # 各服务端口不同
      eager: true                   # 立即初始化
```

## 数据库设计

项目使用独立数据库架构，每个服务对应一个独立数据库：

- `user_db`: 用户数据库
- `product_db`: 商品数据库
- `order_db`: 订单数据库
- `comment_db`: 评论数据库

## 项目结构

```
secondhand-microservices/
├── user-service/
│   ├── src/main/java/com/zjgsu/lll/secondhand/
│   │   ├── client/          # Feign客户端
│   │   │   ├── UserClient.java
│   │   │   └── UserClientFallback.java
│   │   ├── config/          # 配置类
│   │   │   └── LoadBalancerConfig.java
│   │   ├── controller/      # 控制器
│   │   ├── service/         # 业务逻辑
│   │   ├── entity/          # 实体类
│   │   ├── repository/      # 数据访问
│   │   └── common/          # 公共类（Result等）
│   └── src/main/resources/
│       └── application.yml
├── product-service/
│   └── (结构同上)
├── order-service/
│   └── (结构同上)
├── comment-service/
│   └── (结构同上)
└── pom.xml
```

## 前置环境要求

1. **JDK 21**
2. **Maven 3.6+**
3. **MySQL 8.0+**
4. **Nacos 2.2.3**
5. **Sentinel Dashboard 1.8+**（可选，用于监控）

## 快速开始

详细的环境配置和启动步骤请参考 `启动指南.md` 文件。

## API接口文档

### 用户服务 (user-service:8081)

- `GET /users` - 获取所有用户
- `GET /users/{id}` - 根据ID获取用户
- `GET /users/username/{username}` - 根据用户名获取用户
- `POST /users` - 创建用户
- `PUT /users/{id}` - 更新用户
- `DELETE /users/{id}` - 删除用户
- `POST /users/login` - 用户登录

### 商品服务 (product-service:8082)

- `GET /products` - 获取所有商品
- `GET /products/{id}` - 根据ID获取商品
- `GET /products/status/{status}` - 根据状态获取商品
- `GET /products/seller/{sellerId}` - 根据卖家ID获取商品
- `GET /products/category/{category}` - 根据分类获取商品
- `GET /products/search?keyword={keyword}` - 搜索商品
- `POST /products` - 创建商品
- `PUT /products/{id}` - 更新商品
- `DELETE /products/{id}` - 删除商品
- `PUT /products/{id}/status/{status}` - 更新商品状态
- `PUT /products/{id}/reduce-stock/{quantity}` - 减少库存

### 订单服务 (order-service:8083)

- `GET /orders` - 获取所有订单
- `GET /orders/{id}` - 根据ID获取订单
- `GET /orders/orderNo/{orderNo}` - 根据订单号获取订单
- `GET /orders/buyer/{buyerId}` - 根据买家ID获取订单
- `GET /orders/seller/{sellerId}` - 根据卖家ID获取订单
- `GET /orders/status/{status}` - 根据状态获取订单
- `POST /orders` - 创建订单
- `PUT /orders/{id}/pay` - 支付订单
- `PUT /orders/{id}/ship` - 发货
- `PUT /orders/{id}/finish` - 完成订单
- `DELETE /orders/{id}` - 取消订单

### 评论服务 (comment-service:8084)

- `GET /comments` - 获取所有评论
- `GET /comments/{id}` - 根据ID获取评论
- `GET /comments/product/{productId}` - 根据商品ID获取评论
- `GET /comments/user/{userId}` - 根据用户ID获取评论
- `GET /comments/order/{orderId}` - 根据订单ID获取评论
- `POST /comments` - 创建评论
- `DELETE /comments/{id}` - 删除评论

## 测试服务间调用

### 测试场景1：创建订单（调用商品服务）

```bash
# 1. 先创建一个商品
curl -X POST http://localhost:8082/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "二手MacBook Pro",
    "description": "2020款，95成新",
    "price": 8888.00,
    "category": "电子产品",
    "sellerId": 1,
    "stock": 1
  }'

# 2. 创建订单（会调用商品服务获取商品信息）
curl -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "productId": 1,
    "totalPrice": 8888.00,
    "shippingAddress": "浙江省杭州市某某街道",
    "contactPhone": "13800138000"
  }'
```

### 测试场景2：支付订单（调用商品服务减少库存）

```bash
# 支付订单（会调用商品服务减少库存）
curl -X PUT http://localhost:8083/orders/1/pay
```

### 测试场景3：创建评论（调用订单服务验证）

```bash
# 创建评论（会调用订单服务验证订单）
curl -X POST http://localhost:8084/comments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 2,
    "productId": 1,
    "rating": 5,
    "content": "商品很好，卖家服务态度也很棒！"
  }'
```

### 测试熔断降级

```bash
# 1. 停止product-service服务

# 2. 尝试创建订单，会触发降级逻辑
curl -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -d '{...}'

# 预期返回：
# {
#   "code": 500,
#   "message": "商品服务暂时不可用，请稍后重试",
#   "data": null
# }
```

## 负载均衡测试

1. 启动同一服务的多个实例（修改端口）
2. 观察Nacos控制台，确认多个实例已注册
3. 多次调用服务接口
4. 查看日志，确认请求被分发到不同实例（轮询策略）

## 监控

### Nacos控制台

- 访问: http://localhost:8848/nacos
- 用户名/密码: nacos/nacos
- 功能: 查看服务注册情况、实例健康状态

### Sentinel控制台（可选）

- 访问: http://localhost:8080
- 用户名/密码: sentinel/sentinel
- 功能: 实时监控、流控规则、熔断规则

## 常见问题

### 1. Feign调用超时

解决方案：调整`application.yml`中的超时配置
```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connect-timeout: 10000
            read-timeout: 20000
```

### 2. 熔断器未生效

确认以下配置：
- Sentinel依赖已添加
- `spring.cloud.openfeign.sentinel.enabled=true`
- Fallback类已注册为Bean（@Component）

### 3. 服务调用失败

检查：
- 目标服务是否在Nacos中注册成功
- 服务名称是否正确（与application.name一致）
- 网络连接是否正常

## 项目亮点

1. **微服务架构**: 采用Spring Cloud Alibaba生态，实现服务拆分和独立部署
2. **服务间通信**: 使用OpenFeign实现声明式服务调用，代码简洁优雅
3. **负载均衡**: 集成LoadBalancer实现客户端负载均衡，提高系统可用性
4. **熔断降级**: Sentinel熔断保护，保证系统稳定性
5. **服务治理**: Nacos统一管理服务注册发现
6. **独立数据库**: 每个服务独立数据库，符合微服务最佳实践

## 后续规划

- 阶段4: 网关与统一认证
- 阶段5: 分布式事务
- 阶段6: 链路追踪与监控
- 阶段7: 容器化部署（Docker + Kubernetes）

