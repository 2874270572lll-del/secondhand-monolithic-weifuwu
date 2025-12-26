# Nacos配置文件说明

本目录包含所有需要上传到Nacos Config的配置文件。

## 配置文件列表

### 1. 通用配置（common-config.yaml）
所有服务共享的通用配置

### 2. 服务特定配置
- user-service-dev.yaml - 用户服务配置
- product-service-dev.yaml - 商品服务配置
- order-service-dev.yaml - 订单服务配置
- comment-service-dev.yaml - 评论服务配置
- gateway-service-dev.yaml - 网关服务配置

## 如何使用

### 方式1：通过Nacos控制台手动上传

1. 访问 Nacos 控制台：http://localhost:8848/nacos
2. 使用默认账号登录：nacos / nacos
3. 进入"配置管理" -> "配置列表"
4. 点击"+"按钮创建配置
5. 填写配置信息：
   - Data ID：例如 `user-service-dev.yaml`
   - Group：`DEFAULT_GROUP`
   - 配置格式：`YAML`
   - 配置内容：复制对应文件的内容
6. 点击"发布"

### 方式2：使用脚本批量导入

```bash
# 使用提供的导入脚本
./import-nacos-configs.sh
```

## 配置说明

### 命名空间（Namespace）
- dev：开发环境
- test：测试环境
- prod：生产环境

### 配置优先级
1. Nacos Config中的配置（优先级最高）
2. bootstrap.yml中的配置
3. application.yml中的配置（优先级最低）

## 动态刷新
所有标记为 `refresh: true` 的配置支持动态刷新，在Nacos控制台修改配置后会自动推送到服务，无需重启。
