# 测试环境配置文件

这些配置文件用于Nacos的 **test** 命名空间。

## 配置文件列表

- `gateway-service-test.yaml` - 网关服务配置
- `user-service-test.yaml` - 用户服务配置
- `product-service-test.yaml` - 商品服务配置
- `order-service-test.yaml` - 订单服务配置
- `comment-service-test.yaml` - 评论服务配置
- `common-config.yaml` - 公共配置

## 上传到Nacos

登录Nacos控制台 (http://localhost:8848/nacos)，切换到 `test` 命名空间，手动导入这些配置文件。

## 注意事项

1. test环境与dev环境共用数据库
2. test环境服务使用不同的端口（9xxx）
3. 配置文件中的dataId应该以 `-test.yaml` 结尾
