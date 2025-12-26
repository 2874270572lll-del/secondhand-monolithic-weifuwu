# 二手交易平台 - 阶段5：配置中心

## 项目概述

本项目是基于Spring Cloud Alibaba的微服务二手交易平台，已完成**阶段5：配置中心**的实现。

**阶段5目标**：
- ✅ 使用Nacos Config实现配置集中管理
- ✅ 配置从Nacos动态读取
- ✅ 支持配置动态刷新（无需重启服务）
- ✅ 实现多环境配置隔离（dev/test/prod）

## 技术架构

### 核心技术栈
- **Spring Boot**: 3.3.6
- **Spring Cloud**: 2023.0.3
- **Spring Cloud Alibaba**: 2023.0.1.2
- **Nacos Server**: v3.1.0（服务注册与配置中心）
- **MySQL**: 8.4
- **Java**: 21

### 微服务列表
1. **user-service** (8081) - 用户服务
2. **product-service** (8082) - 商品服务
3. **order-service** (8083) - 订单服务
4. **comment-service** (8084) - 评论服务
5. **gateway-service** (8080) - API网关

## 阶段5新增功能

### 1. Nacos Config集成
- 所有微服务集成Nacos Config
- 添加`spring-cloud-starter-alibaba-nacos-config`依赖
- 创建`bootstrap.yml`配置文件
- 配置优先级：Nacos Config > Bootstrap > Application

### 2. 配置文件结构
```
nacos-configs/
├── common-config.yaml          # 通用配置（所有服务共享）
├── user-service-dev.yaml       # 用户服务配置
├── product-service-dev.yaml    # 商品服务配置
├── order-service-dev.yaml      # 订单服务配置
├── comment-service-dev.yaml    # 评论服务配置
└── gateway-service-dev.yaml    # 网关服务配置
```

### 3. 动态刷新功能
- 使用`@RefreshScope`注解支持配置动态刷新
- 在Nacos控制台修改配置后自动推送到服务
- 无需重启服务即可生效

### 4. 多环境支持
- **dev**: 开发环境
- **test**: 测试环境
- **prod**: 生产环境
- 通过namespace实现环境隔离

## 快速开始

### 前置要求
- JDK 21
- Maven 3.8+
- Docker 和 Docker Compose（可选，用于容器化部署）
- MySQL 8.4

### 步骤1：启动基础设施

#### 方式1：使用Docker Compose（推荐）
```bash
# 启动Nacos和MySQL
docker-compose up -d secondhand-mysql secondhand-nacos

# 查看服务状态
docker-compose ps

# 等待Nacos启动完成（约30秒）
docker-compose logs -f secondhand-nacos
```

#### 方式2：本地安装
参考[启动指南-阶段4.md](启动指南-阶段4.md)中的说明

### 步骤2：配置Nacos Config

#### 2.1 访问Nacos控制台
- URL: http://localhost:8848/nacos
- 默认账号: nacos
- 默认密码: nacos

#### 2.2 创建命名空间
1. 进入"命名空间"菜单
2. 点击"新建命名空间"
3. 命名空间ID: `dev`
4. 命名空间名: `开发环境`
5. 点击"确定"

#### 2.3 导入配置文件
在"配置管理" -> "配置列表"中，选择`dev`命名空间，依次创建以下配置：

| Data ID | Group | 格式 | 配置文件 |
|---------|-------|------|----------|
| common-config.yaml | DEFAULT_GROUP | YAML | nacos-configs/common-config.yaml |
| user-service-dev.yaml | DEFAULT_GROUP | YAML | nacos-configs/user-service-dev.yaml |
| product-service-dev.yaml | DEFAULT_GROUP | YAML | nacos-configs/product-service-dev.yaml |
| order-service-dev.yaml | DEFAULT_GROUP | YAML | nacos-configs/order-service-dev.yaml |
| comment-service-dev.yaml | DEFAULT_GROUP | YAML | nacos-configs/comment-service-dev.yaml |
| gateway-service-dev.yaml | DEFAULT_GROUP | YAML | nacos-configs/gateway-service-dev.yaml |

**创建配置步骤**：
1. 点击"+"按钮
2. 填写Data ID（如：`user-service-dev.yaml`）
3. 选择Group：`DEFAULT_GROUP`
4. 选择配置格式：`YAML`
5. 复制`nacos-configs`目录中对应文件的内容到配置内容框
6. 点击"发布"

### 步骤3：编译项目
```bash
# 编译所有服务
mvn clean package -DskipTests

# 或者编译单个服务
cd user-service
mvn clean package -DskipTests
```

### 步骤4：启动微服务

#### 方式1：使用Docker Compose启动所有服务
```bash
# 启动所有微服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f user-service
```

#### 方式2：使用Java命令启动
```bash
# 启动用户服务
java -jar user-service/target/user-service-1.0.0.jar

# 启动商品服务
java -jar product-service/target/product-service-1.0.0.jar

# 启动订单服务
java -jar order-service/target/order-service-1.0.0.jar

# 启动评论服务
java -jar comment-service/target/comment-service-1.0.0.jar

# 启动网关服务
java -jar gateway-service/target/gateway-service-1.0.0.jar
```

### 步骤5：验证配置中心功能

#### 5.1 检查服务注册
访问 Nacos 控制台的"服务管理" -> "服务列表"，确认所有服务已注册。

#### 5.2 测试配置读取
```bash
# 测试配置读取接口
curl http://localhost:8081/api/config/current
```

响应示例：
```json
{
  "featureEnabled": true,
  "maxPageSize": 50,
  "welcomeMessage": "欢迎使用二手交易平台 - 配置中心版",
  "jwtExpiration": 86400000,
  "jwtExpirationHours": 24,
  "message": "配置从Nacos Config动态读取",
  "refreshHint": "在Nacos控制台修改配置后，此接口将返回最新值（无需重启服务）"
}
```

#### 5.3 测试动态刷新

**步骤**：
1. 记录当前配置值：
   ```bash
   curl http://localhost:8081/api/config/current
   ```

2. 在Nacos控制台修改配置：
   - 进入"配置管理" -> "配置列表"
   - 找到`common-config.yaml`
   - 点击"编辑"
   - 修改`business.welcome-message`的值
   - 点击"发布"

3. 等待3-5秒后再次访问：
   ```bash
   curl http://localhost:8081/api/config/current
   ```

4. 观察`welcomeMessage`字段的值已更新（**无需重启服务**）

## 配置说明

### 配置优先级
1. **Nacos Config** (最高优先级)
2. **bootstrap.yml**
3. **application.yml** (最低优先级)

### 配置加载流程
1. 服务启动时，首先加载`bootstrap.yml`
2. 根据`bootstrap.yml`中的配置连接到Nacos
3. 从Nacos加载配置：
   - 共享配置：`common-config.yaml`
   - 服务特定配置：`{service-name}-{profile}.yaml`
4. 合并本地`application.yml`配置

### 动态刷新机制
- 标记`@RefreshScope`的Bean支持动态刷新
- Nacos推送配置变更通知
- Spring Cloud自动刷新Bean
- 新的请求使用最新配置

## API测试

### 1. 配置查询接口
```bash
# 获取当前配置
curl http://localhost:8081/api/config/current

# 获取功能开关状态
curl http://localhost:8081/api/config/feature-status

# 获取欢迎消息
curl http://localhost:8081/api/config/welcome
```

### 2. 用户服务接口
```bash
# 用户注册
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'

# 用户登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 3. 商品服务接口
```bash
# 获取商品列表
curl http://localhost:8080/api/product/list

# 获取商品详情
curl http://localhost:8080/api/product/1/detail
```

## 监控与日志

### Nacos控制台
- URL: http://localhost:8848/nacos
- 功能：
  - 服务列表查看
  - 配置管理
  - 配置历史版本
  - 配置监听查询

### 应用日志
```bash
# 查看user-service日志
docker-compose logs -f user-service

# 查看所有服务日志
docker-compose logs -f
```

### 配置变更历史
在Nacos控制台的"配置管理" -> "历史版本"中可以查看配置的变更历史，支持版本回滚。

## 常见问题

### Q1: 服务启动失败，提示无法连接Nacos
**解决方案**：
1. 确认Nacos服务已启动：`docker-compose ps`
2. 检查Nacos日志：`docker-compose logs secondhand-nacos`
3. 确认网络连通性：`ping secondhand-nacos`（容器内）或 `ping localhost`（主机）

### Q2: 配置不生效
**解决方案**：
1. 检查Nacos中配置的Data ID是否正确
2. 确认Group和Namespace配置正确
3. 查看服务日志，确认配置加载成功
4. 确保Bean标记了`@RefreshScope`注解

### Q3: 配置修改后不刷新
**解决方案**：
1. 确认配置中`refresh-enabled: true`
2. 检查Bean是否标记`@RefreshScope`
3. 查看Nacos推送日志
4. 尝试重启服务

### Q4: 多环境配置如何切换
**解决方案**：
```bash
# 启动时指定profile
java -jar user-service.jar --spring.profiles.active=test

# 或通过环境变量
export SPRING_PROFILES_ACTIVE=prod
java -jar user-service.jar
```

## 项目结构

```
secondhand-microservices/
├── user-service/               # 用户服务
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │           ├── application.yml
│   │           └── bootstrap.yml  # ✨新增
│   └── pom.xml                    # ✨已更新
├── product-service/            # 商品服务
├── order-service/              # 订单服务
├── comment-service/            # 评论服务
├── gateway-service/            # 网关服务
├── nacos-configs/              # ✨新增：Nacos配置文件目录
│   ├── README.md
│   ├── common-config.yaml
│   ├── user-service-dev.yaml
│   ├── product-service-dev.yaml
│   ├── order-service-dev.yaml
│   ├── comment-service-dev.yaml
│   └── gateway-service-dev.yaml
├── docker-compose.yml
├── pom.xml
└── README-阶段5.md             # ✨本文件
```

## 技术亮点

### 1. 配置集中管理
- 所有服务配置统一存储在Nacos
- 便于配置的统一管理和维护
- 支持配置版本管理和回滚

### 2. 动态配置刷新
- 使用`@RefreshScope`实现配置热更新
- 无需重启服务即可生效
- 提高系统可用性和运维效率

### 3. 多环境支持
- 通过Namespace实现环境隔离
- dev/test/prod环境配置独立
- 防止配置误操作影响生产环境

### 4. 配置共享与继承
- common-config.yaml包含通用配置
- 服务特定配置继承通用配置
- 减少配置冗余，便于维护

## 下一步计划

### 阶段6：分布式事务（可选）
- [ ] 集成Seata实现分布式事务
- [ ] TCC模式实现订单创建事务
- [ ] 测试事务回滚机制

### 阶段7：链路追踪（可选）
- [ ] 集成Sleuth和Zipkin
- [ ] 实现全链路追踪
- [ ] 可视化服务调用链

### 阶段8：服务监控（可选）
- [ ] 集成Prometheus和Grafana
- [ ] 添加业务指标监控
- [ ] 配置告警规则

## 参考资料

- [Nacos官方文档](https://nacos.io/zh-cn/docs/quick-start.html)
- [Spring Cloud Alibaba文档](https://github.com/alibaba/spring-cloud-alibaba/wiki)
- [Spring Cloud Config文档](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)

## 作者

- 姓名：[你的姓名]
- 学号：[你的学号]
- 日期：2025年12月23日

## 许可证

本项目仅用于学习和教学目的。
