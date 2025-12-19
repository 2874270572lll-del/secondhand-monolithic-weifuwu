# 二手交易平台 - 单体版

基于Spring Boot 3的二手交易平台单体应用，包含用户管理、商品管理、订单管理和评论系统。

## 技术栈

- **Java 17+** (项目配置为Java 25，可根据实际情况调整)
- **Spring Boot 3.3.6**
- **Spring Data JPA**
- **MySQL 8.0+**
- **Lombok**
- **Hibernate**
- **Maven**

## 项目结构

```
secondhand-monolithic/
├── src/
│   ├── main/
│   │   ├── java/com/zjgsu/lll/secondhand/
│   │   │   ├── SecondhandMonolithicApplication.java  # 主应用类
│   │   │   ├── entity/                                # 实体类
│   │   │   │   ├── User.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── Order.java
│   │   │   │   └── Comment.java
│   │   │   ├── repository/                            # 数据访问层
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── CommentRepository.java
│   │   │   ├── service/                               # 业务逻辑层
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   └── CommentService.java
│   │   │   ├── controller/                            # 控制器层
│   │   │   │   ├── UserController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── CommentController.java
│   │   │   ├── common/                                # 通用类
│   │   │   │   └── Result.java
│   │   │   └── exception/                             # 异常处理
│   │   │       ├── BusinessException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.yml                        # 配置文件
│   └── test/                                          # 测试代码
├── pom.xml                                            # Maven配置
├── 测试流程.md                                         # 详细测试文档
├── Postman_Collection.json                            # Postman测试集合
└── README.md                                          # 本文件
```

## 核心功能

### 1. 用户管理
- 用户注册
- 用户登录
- 用户信息查询和更新
- 用户状态管理

### 2. 商品管理
- 商品发布
- 商品编辑
- 商品上下架
- 商品搜索（关键词、类别）
- 库存管理

### 3. 订单管理
- 订单创建
- 订单支付
- 订单发货
- 订单确认收货
- 订单状态流转（待支付 -> 已支付 -> 已发货 -> 已完成）
- 订单取消

### 4. 评论系统
- 发布评论（仅已完成订单）
- 查看商品评论
- 评分系统（1-5星）

## 快速开始

### 前置要求

1. JDK 17 或更高版本
2. Maven 3.6+
3. MySQL 8.0+
4. Postman 或其他API测试工具（可选）

### 1. 创建数据库

```sql
CREATE DATABASE secondhand_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/secondhand_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root      # 修改为你的数据库用户名
    password: root      # 修改为你的数据库密码
```

### 3. 调整Java版本（如果需要）

如果你的JDK版本不是25，请修改 `pom.xml` 中的Java版本：

```xml
<properties>
    <java.version>17</java.version>  <!-- 改为你的JDK版本 -->
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

### 4. 编译项目

```bash
cd secondhand-monolithic
mvn clean install
```

### 5. 运行项目

```bash
mvn spring-boot:run
```

或者在IDE中直接运行 `SecondhandMonolithicApplication` 主类。

### 6. 验证启动

启动成功后，访问以下URL验证：

- 应用基础路径：`http://localhost:8080/api`
- 测试接口：`http://localhost:8080/api/users`

## API文档

### 基础信息
- **Base URL**: `http://localhost:8080/api`
- **数据格式**: JSON
- **字符编码**: UTF-8

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### API端点概览

#### 用户管理 (/users)
- `POST /users` - 注册用户
- `POST /users/login` - 用户登录
- `GET /users` - 获取所有用户
- `GET /users/{id}` - 根据ID获取用户
- `GET /users/username/{username}` - 根据用户名获取用户
- `PUT /users/{id}` - 更新用户信息
- `DELETE /users/{id}` - 删除用户

#### 商品管理 (/products)
- `POST /products` - 发布商品
- `GET /products` - 获取所有商品
- `GET /products/{id}` - 根据ID获取商品
- `GET /products/status/{status}` - 根据状态获取商品
- `GET /products/seller/{sellerId}` - 获取卖家的商品
- `GET /products/category/{category}` - 根据类别获取商品
- `GET /products/search?keyword=xxx` - 搜索商品
- `PUT /products/{id}` - 更新商品
- `PUT /products/{id}/status/{status}` - 更新商品状态
- `DELETE /products/{id}` - 删除商品

#### 订单管理 (/orders)
- `POST /orders` - 创建订单
- `GET /orders` - 获取所有订单
- `GET /orders/{id}` - 根据ID获取订单
- `GET /orders/orderNo/{orderNo}` - 根据订单号获取订单
- `GET /orders/buyer/{buyerId}` - 获取买家订单
- `GET /orders/seller/{sellerId}` - 获取卖家订单
- `GET /orders/status/{status}` - 根据状态获取订单
- `PUT /orders/{id}/pay` - 支付订单
- `PUT /orders/{id}/ship` - 发货
- `PUT /orders/{id}/finish` - 确认收货
- `DELETE /orders/{id}` - 取消订单

#### 评论管理 (/comments)
- `POST /comments` - 发布评论
- `GET /comments` - 获取所有评论
- `GET /comments/{id}` - 根据ID获取评论
- `GET /comments/product/{productId}` - 获取商品评论
- `GET /comments/user/{userId}` - 获取用户评论
- `GET /comments/order/{orderId}` - 获取订单评论
- `DELETE /comments/{id}` - 删除评论

## 测试指南

### 使用Postman测试

1. 导入 `Postman_Collection.json` 文件到Postman
2. 按照文件夹顺序执行测试：用户管理 -> 商品管理 -> 订单管理 -> 评论管理
3. 注意修改请求中的ID等参数

### 完整业务流程测试

详细测试步骤请参考：[测试流程.md](./测试流程.md)

完整流程：
1. 注册卖家和买家账户
2. 用户登录
3. 卖家发布商品
4. 买家浏览和搜索商品
5. 买家创建订单
6. 买家支付订单
7. 卖家发货
8. 买家确认收货
9. 买家发布评论

## 数据库表结构

### users (用户表)
- id: 主键
- username: 用户名（唯一）
- password: 密码
- email: 邮箱（唯一）
- phone: 手机号
- address: 地址
- status: 状态（1-正常，0-禁用）
- create_time: 创建时间
- update_time: 更新时间

### products (商品表)
- id: 主键
- name: 商品名称
- description: 商品描述
- price: 价格
- category: 分类
- stock: 库存
- image_url: 图片URL
- status: 状态（1-上架，0-下架，2-已售出）
- seller_id: 卖家ID
- create_time: 创建时间
- update_time: 更新时间

### orders (订单表)
- id: 主键
- order_no: 订单号（唯一）
- buyer_id: 买家ID
- seller_id: 卖家ID
- product_id: 商品ID
- total_amount: 总金额
- status: 状态（0-待支付，1-已支付，2-已发货，3-已完成，4-已取消）
- shipping_address: 收货地址
- contact_phone: 联系电话
- pay_time: 支付时间
- ship_time: 发货时间
- finish_time: 完成时间
- create_time: 创建时间
- update_time: 更新时间

### comments (评论表)
- id: 主键
- product_id: 商品ID
- user_id: 用户ID
- order_id: 订单ID
- content: 评论内容
- rating: 评分（1-5）
- create_time: 创建时间

## 常见问题

### 1. 启动失败：数据库连接失败
- 检查MySQL是否正常运行
- 检查数据库名称、用户名、密码是否正确
- 检查MySQL端口是否为3306

### 2. 启动失败：端口被占用
- 修改 `application.yml` 中的 `server.port` 为其他端口

### 3. 编译失败：Java版本不匹配
- 修改 `pom.xml` 中的 `java.version` 为你安装的JDK版本

### 4. Hibernate自动建表失败
- 检查数据库用户是否有创建表的权限
- 检查 `application.yml` 中 `jpa.hibernate.ddl-auto` 配置

## 开发建议

### 后续改进方向

1. **安全性增强**
   - 添加Spring Security进行认证授权
   - 密码使用BCrypt加密
   - 添加JWT Token认证

2. **功能扩展**
   - 添加支付集成
   - 添加文件上传功能
   - 添加消息通知系统
   - 添加订单退款功能

3. **性能优化**
   - 添加Redis缓存
   - 添加分页查询
   - 添加数据库索引

4. **代码质量**
   - 添加单元测试
   - 添加API文档（Swagger/OpenAPI）
   - 添加日志记录

## 许可证

本项目仅供学习使用。

## 联系方式

如有问题，请通过以下方式联系：
- 提交Issue
- 发送邮件
