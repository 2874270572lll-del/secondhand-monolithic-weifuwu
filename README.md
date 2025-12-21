# 二手交易平台微服务架构

本项目是从单体应用（secondhand-monolithic）拆分而来的微服务架构实现。

## 项目结构

```
secondhand-microservices/
├── pom.xml                    # 父级POM文件
├── user-service/             # 用户服务 (端口: 8081)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/zjgsu/lll/secondhand/
│       │   ├── UserServiceApplication.java
│       │   ├── entity/User.java
│       │   ├── repository/UserRepository.java
│       │   ├── service/UserService.java
│       │   ├── controller/UserController.java
│       │   ├── common/Result.java
│       │   └── exception/
│       └── resources/application.yml
│
├── product-service/          # 商品服务 (端口: 8082)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/zjgsu/lll/secondhand/
│       │   ├── ProductServiceApplication.java
│       │   ├── entity/Product.java
│       │   ├── repository/ProductRepository.java
│       │   ├── service/ProductService.java
│       │   ├── controller/ProductController.java
│       │   ├── common/Result.java
│       │   └── exception/
│       └── resources/application.yml
│
├── order-service/            # 订单服务 (端口: 8083)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/zjgsu/lll/secondhand/
│       │   ├── OrderServiceApplication.java
│       │   ├── entity/Order.java
│       │   ├── repository/OrderRepository.java
│       │   ├── service/OrderService.java
│       │   ├── controller/OrderController.java
│       │   ├── client/ProductClient.java    # Feign客户端
│       │   ├── common/Result.java
│       │   └── exception/
│       └── resources/application.yml
│
└── comment-service/          # 评论服务 (端口: 8084)
    ├── pom.xml
    └── src/main/
        ├── java/com/zjgsu/lll/secondhand/
        │   ├── CommentServiceApplication.java
        │   ├── entity/Comment.java
        │   ├── repository/CommentRepository.java
        │   ├── service/CommentService.java
        │   ├── controller/CommentController.java
        │   ├── client/OrderClient.java      # Feign客户端
        │   ├── common/Result.java
        │   └── exception/
        └── resources/application.yml
```

## 微服务说明

### 1. user-service (用户服务)
- **端口**: 8081
- **数据库**: user_db
- **功能**:
  - 用户注册、登录
  - 用户信息管理
  - 用户状态管理

### 2. product-service (商品服务)
- **端口**: 8082
- **数据库**: product_db
- **功能**:
  - 商品发布、编辑、删除
  - 商品查询（按状态、分类、卖家、关键词）
  - 库存管理
  - 商品状态管理

### 3. order-service (订单服务)
- **端口**: 8083
- **数据库**: order_db
- **功能**:
  - 订单创建
  - 订单支付、发货、完成、取消
  - 订单查询（按买家、卖家、状态）
  - **服务调用**: 通过Feign调用product-service获取商品信息和减库存

### 4. comment-service (评论服务)
- **端口**: 8084
- **数据库**: comment_db
- **功能**:
  - 评论创建、删除
  - 评论查询（按商品、用户、订单）
  - 评分管理（1-5星）
  - **服务调用**: 通过Feign调用order-service验证订单

## 技术栈

- **Spring Boot**: 3.3.6
- **Spring Cloud**: 2023.0.3
- **Spring Cloud Alibaba**: 2023.0.1.2
- **Nacos**: 服务注册与发现
- **OpenFeign**: 服务间调用
- **Spring Data JPA**: 数据持久化
- **MySQL**: 8.4.0
- **Java**: 25

## 环境准备

### 1. 安装Nacos
下载并启动Nacos服务器：
```bash
# Windows
cd nacos/bin
startup.cmd -m standalone

# Linux/Mac
cd nacos/bin
sh startup.sh -m standalone
```
访问: http://localhost:8848/nacos (用户名/密码: nacos/nacos)

### 2. 创建数据库
在MySQL中创建4个数据库：
```sql
CREATE DATABASE user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE product_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE comment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 修改数据库配置
如果MySQL密码不是 `lilili2004`，需要修改各服务的 `application.yml` 文件中的数据库密码。

## 启动服务

### 方式1: 使用Maven命令
```bash
# 在项目根目录编译所有服务
mvn clean install

# 启动各个服务
cd user-service
mvn spring-boot:run

cd product-service
mvn spring-boot:run

cd order-service
mvn spring-boot:run

cd comment-service
mvn spring-boot:run
```

### 方式2: 使用IDE
在IDEA中分别启动各服务的Application类：
- UserServiceApplication
- ProductServiceApplication
- OrderServiceApplication
- CommentServiceApplication

## 服务启动顺序建议

1. 确保Nacos已启动
2. 启动user-service (8081)
3. 启动product-service (8082)
4. 启动order-service (8083) - 依赖product-service
5. 启动comment-service (8084) - 依赖order-service

## API接口说明

### User Service (8081)
- `POST /users` - 创建用户
- `GET /users` - 获取所有用户
- `GET /users/{id}` - 根据ID获取用户
- `GET /users/username/{username}` - 根据用户名获取用户
- `PUT /users/{id}` - 更新用户
- `DELETE /users/{id}` - 删除用户
- `POST /users/login` - 用户登录

### Product Service (8082)
- `POST /products` - 创建商品
- `GET /products` - 获取所有商品
- `GET /products/{id}` - 根据ID获取商品
- `GET /products/status/{status}` - 根据状态获取商品
- `GET /products/seller/{sellerId}` - 根据卖家获取商品
- `GET /products/category/{category}` - 根据分类获取商品
- `GET /products/search?keyword=xxx` - 搜索商品
- `PUT /products/{id}` - 更新商品
- `DELETE /products/{id}` - 删除商品
- `PUT /products/{id}/status/{status}` - 更新商品状态
- `PUT /products/{id}/reduce-stock/{quantity}` - 减少库存

### Order Service (8083)
- `POST /orders` - 创建订单
- `GET /orders` - 获取所有订单
- `GET /orders/{id}` - 根据ID获取订单
- `GET /orders/orderNo/{orderNo}` - 根据订单号获取订单
- `GET /orders/buyer/{buyerId}` - 根据买家获取订单
- `GET /orders/seller/{sellerId}` - 根据卖家获取订单
- `GET /orders/status/{status}` - 根据状态获取订单
- `PUT /orders/{id}/pay` - 支付订单
- `PUT /orders/{id}/ship` - 发货
- `PUT /orders/{id}/finish` - 完成订单
- `DELETE /orders/{id}` - 取消订单

### Comment Service (8084)
- `POST /comments` - 创建评论
- `GET /comments` - 获取所有评论
- `GET /comments/{id}` - 根据ID获取评论
- `GET /comments/product/{productId}` - 根据商品获取评论
- `GET /comments/user/{userId}` - 根据用户获取评论
- `GET /comments/order/{orderId}` - 根据订单获取评论
- `DELETE /comments/{id}` - 删除评论

## 服务间调用关系

```
┌─────────────────┐
│  user-service   │ (独立服务)
└─────────────────┘

┌─────────────────┐
│ product-service │ (独立服务)
└─────────────────┘
         ↑
         │ Feign调用
         │
┌─────────────────┐
│  order-service  │ (调用product-service)
└─────────────────┘
         ↑
         │ Feign调用
         │
┌─────────────────┐
│ comment-service │ (调用order-service)
└─────────────────┘
```

## 配置说明

每个服务的 `application.yml` 配置项：
- `server.port`: 服务端口
- `spring.application.name`: 服务名称（用于Nacos注册）
- `spring.datasource`: 数据库连接配置
- `spring.cloud.nacos.discovery`: Nacos配置
- `spring.cloud.openfeign`: Feign客户端配置（order-service和comment-service）

## 注意事项

1. **数据库密码**: 默认密码为 `lilili2004`，请根据实际情况修改
2. **Java版本**: 项目使用Java 25，确保环境匹配
3. **Nacos**: 必须先启动Nacos，否则服务无法注册
4. **服务依赖**: order-service依赖product-service，comment-service依赖order-service
5. **数据一致性**: 目前未实现分布式事务，需要注意数据一致性问题

## 监控和管理

访问Nacos控制台查看服务注册情况：
- URL: http://localhost:8848/nacos
- 用户名: nacos
- 密码: nacos

在"服务管理" -> "服务列表"中可以看到所有注册的微服务。

## 后续优化建议

1. **网关**: 添加Spring Cloud Gateway统一入口
2. **配置中心**: 使用Nacos Config管理配置
3. **分布式事务**: 使用Seata处理分布式事务
4. **限流降级**: 使用Sentinel实现流量控制
5. **链路追踪**: 使用Sleuth + Zipkin实现链路追踪
6. **消息队列**: 使用RocketMQ解耦服务
7. **缓存**: 添加Redis缓存热点数据
8. **Docker**: 容器化部署
9. **Kubernetes**: 编排和管理容器
