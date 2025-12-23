# 二手交易平台微服务 - 阶段4：API网关与统一认证

## 阶段概述

本阶段实现了API网关和JWT统一认证功能，所有客户端请求统一通过Gateway进行路由转发和身份验证。

### 完成功能

✅ Spring Cloud Gateway作为统一入口
✅ JWT认证机制
✅ 路由转发与负载均衡
✅ 认证白名单配置
✅ 全局CORS跨域配置
✅ 用户登录与注册接口

---

## 架构说明

### 整体架构

```
客户端
  ↓
API Gateway (8080)
  ├── JWT认证过滤器
  ├── 路由转发
  └── 负载均衡
  ↓
后端微服务
  ├── user-service (8081)
  ├── product-service (8082)
  ├── order-service (8083)
  └── comment-service (8084)
```

### 认证流程

1. **用户登录**: `POST /auth/login` → 返回JWT Token
2. **访问受保护资源**: 请求Header携带 `Authorization: Bearer {token}`
3. **Gateway验证**: JWT认证过滤器验证token有效性
4. **转发请求**: 验证通过后转发到后端服务
5. **返回结果**: 后端处理完成后返回给客户端

---

## 项目结构

### Gateway服务

```
gateway-service/
├── src/main/java/
│   └── com/zjgsu/lll/secondhand/gateway/
│       ├── GatewayServiceApplication.java     # 启动类
│       ├── filter/
│       │   └── JwtAuthenticationFilter.java    # JWT认证过滤器
│       └── util/
│           └── JwtUtil.java                    # JWT工具类
├── src/main/resources/
│   └── application.yml                         # 网关配置
└── pom.xml
```

### User服务（新增认证功能）

```
user-service/
└── src/main/java/
    └── com/zjgsu/lll/secondhand/
        ├── controller/
        │   └── AuthController.java             # 认证控制器
        ├── dto/
        │   ├── LoginRequest.java               # 登录请求DTO
        │   └── LoginResponse.java              # 登录响应DTO
        └── util/
            └── JwtUtil.java                    # JWT工具类
```

---

## 核心配置

### Gateway配置 (gateway-service/application.yml)

```yaml
spring:
  application:
    name: gateway-service

  cloud:
    nacos:
      discovery:
        server-addr: secondhand-nacos:8848
        namespace: dev

    gateway:
      # 启用服务发现路由
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

      # 路由配置
      routes:
        # 用户服务路由
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/user/**
          filters:
            - StripPrefix=1

        # 商品服务路由
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/product/**
          filters:
            - StripPrefix=1

        # 订单服务路由
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/order/**
          filters:
            - StripPrefix=1

        # 评论服务路由
        - id: comment-service
          uri: lb://comment-service
          predicates:
            - Path=/comment/**
          filters:
            - StripPrefix=1

        # 认证服务路由（白名单）
        - id: auth-service
          uri: lb://user-service
          predicates:
            - Path=/auth/**
          filters:
            - StripPrefix=0

      # 全局CORS配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowed-headers: "*"
            allow-credentials: true
            max-age: 3600

server:
  port: 8080

# JWT配置
jwt:
  secret: secondhand-platform-jwt-secret-key-2024-very-secure
  expiration: 86400000  # 24小时（毫秒）

# 白名单配置（不需要JWT验证的路径）
auth:
  whitelist:
    - /auth/**
    - /actuator/**
```

---

## API文档

### 1. 用户注册

**接口**: `POST /auth/register`

**请求示例**:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "password": "123456",
    "email": "zhangsan@example.com",
    "phone": "13800138001",
    "address": "浙江省杭州市"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "phone": "13800138001",
    "address": "浙江省杭州市",
    "status": 1,
    "createTime": "2024-12-23T10:30:00"
  }
}
```

### 2. 用户登录

**接口**: `POST /auth/login`

**请求示例**:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "password": "123456"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ6aGFuZ3NhbiIsImlhdCI6MTYzMzk2ODAwMCwiZXhwIjoxNjM0MDU0NDAwfQ.abc123...",
    "username": "zhangsan",
    "userId": 1,
    "expiresIn": 86400000
  }
}
```

### 3. 访问受保护资源

**接口**: `GET /user/users/{id}`

**请求示例**:
```bash
curl http://localhost:8080/user/users/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "email": "zhangsan@example.com",
    "phone": "13800138001"
  }
}
```

---

## 路由规则说明

### 路由映射

| 原始路径 | Gateway路径 | 目标服务 | 说明 |
|---------|------------|----------|------|
| `/users/*` | `/user/users/*` | user-service | 用户管理 |
| `/products/*` | `/product/products/*` | product-service | 商品管理 |
| `/orders/*` | `/order/orders/*` | order-service | 订单管理 |
| `/comments/*` | `/comment/comments/*` | comment-service | 评论管理 |
| `/auth/*` | `/auth/*` | user-service | 认证服务（白名单） |

### 认证白名单

以下路径**不需要JWT认证**，可直接访问：
- `/auth/**` - 认证相关接口（登录、注册）
- `/actuator/**` - 监控端点

其他所有路径都需要在请求Header中携带有效的JWT Token。

---

## 测试指南

### 测试环境准备

1. **确保所有服务已启动**:
   - Nacos (8848)
   - MySQL (3306)
   - gateway-service (8080)
   - user-service (8081)
   - product-service (8082)
   - order-service (8083)
   - comment-service (8084)

2. **检查服务注册**:
   访问 http://localhost:8848/nacos，确认所有服务都已注册且状态健康。

### 测试流程

#### 步骤1：注册新用户

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "email": "test@example.com",
    "phone": "13900139000",
    "address": "测试地址"
  }'
```

#### 步骤2：登录获取Token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }'
```

保存返回的token，例如：
```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYzMzk2ODAwMCwiZXhwIjoxNjM0MDU0NDAwfQ...
```

#### 步骤3：测试未携带Token访问（预期失败）

```bash
curl http://localhost:8080/user/users/1
```

**预期响应**（401 Unauthorized）:
```json
{
  "code": 401,
  "message": "未授权，请先登录"
}
```

#### 步骤4：测试携带Token访问（预期成功）

```bash
curl http://localhost:8080/user/users/1 \
  -H "Authorization: Bearer {your-token-here}"
```

**预期响应**（200 OK）:
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

#### 步骤5：测试白名单路径（无需Token）

```bash
curl http://localhost:8080/auth/test
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "Auth service is running!"
}
```

---

## 常见问题

### 1. 401 Unauthorized错误

**问题**: 访问受保护资源时返回401

**排查步骤**:
1. 检查是否携带了Authorization header
2. 检查Token格式是否正确（`Bearer {token}`）
3. 检查Token是否已过期（有效期24小时）
4. 查看Gateway日志确认JWT验证详情

### 2. 路由404错误

**问题**: 请求返回404 Not Found

**排查步骤**:
1. 检查请求路径是否正确（参考路由映射表）
2. 确认目标服务已启动并注册到Nacos
3. 查看Gateway日志中的路由匹配信息

### 3. 服务调用失败

**问题**: Gateway正常但无法调用后端服务

**排查步骤**:
1. 检查Nacos中服务注册状态
2. 确认后端服务端口正确
3. 查看后端服务日志确认是否收到请求

### 4. CORS跨域错误

**问题**: 浏览器提示跨域错误

**解决方案**:
- Gateway已配置全局CORS，允许所有来源
- 如果仍有问题，检查浏览器控制台的具体错误信息
- 确认OPTIONS预检请求是否正常

---

## 技术要点

### JWT认证机制

1. **Token生成**: 使用HS512算法签名
2. **Token内容**: 包含用户名(subject)、签发时间(iat)、过期时间(exp)
3. **Token验证**: 验证签名、有效期、格式
4. **Token传递**: 通过`Authorization: Bearer {token}`请求头

### 过滤器执行顺序

Gateway的过滤器链执行顺序（order值越小越优先）:

```
JwtAuthenticationFilter (order=-100)
  ↓
StripPrefix Filter
  ↓
LoadBalancer Filter
  ↓
Forward to Backend Service
```

### 安全注意事项

1. **Secret密钥**: 生产环境应使用环境变量配置，不要硬编码
2. **HTTPS**: 生产环境必须使用HTTPS，防止Token被窃取
3. **Token刷新**: 当前实现为固定过期时间，可扩展实现刷新机制
4. **密码加密**: 当前密码为明文存储，生产环境应使用BCrypt等加密

---

## 下一步扩展

### 可选功能

- [ ] Token刷新机制
- [ ] 密码加密存储（BCrypt）
- [ ] 基于角色的权限控制（RBAC）
- [ ] 请求限流
- [ ] API访问日志
- [ ] 黑名单Token机制（登出）

---

## 总结

阶段4成功实现了：

1. ✅ **统一入口**: 所有请求通过Gateway (8080端口)
2. ✅ **JWT认证**: 安全的token认证机制
3. ✅ **路由转发**: 自动路由到后端微服务
4. ✅ **负载均衡**: 集成Spring Cloud LoadBalancer
5. ✅ **白名单**: 灵活的认证白名单配置
6. ✅ **CORS支持**: 跨域请求支持

完成阶段4后，系统已具备完整的API网关和统一认证能力，为后续的功能扩展和生产部署打下坚实基础。

---

**下一阶段预告**: 配置中心、分布式链路追踪、服务监控等高级特性
