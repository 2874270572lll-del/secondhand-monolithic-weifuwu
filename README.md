# 阶段6：异步消息通信 - 技术文档

## 概述

本阶段在**阶段5：配置中心**的基础上，引入RabbitMQ实现异步消息通信。当订单创建成功后，系统会异步发送消息到消息队列，由消费者处理通知逻辑（如向买家和卖家发送通知）。

## 技术架构

### 消息流程图

```
┌─────────────┐
│   客户端     │
└──────┬──────┘
       │ POST /api/order/orders
       ▼
┌─────────────────────────────────────────┐
│          API Gateway (8080)              │
│            JWT认证 + 路由转发             │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────┐
│        Order Service (8083)               │
│  ┌────────────────────────────────────┐  │
│  │ 1. 调用ProductClient获取商品信息   │  │
│  │ 2. 生成订单并保存到数据库          │  │
│  │ 3. 创建OrderCreatedEvent           │  │
│  │ 4. 发送消息到RabbitMQ              │  │
│  └────────────────┬───────────────────┘  │
└───────────────────┼──────────────────────┘
                    │ OrderCreatedEvent
                    ▼
┌──────────────────────────────────────────┐
│         RabbitMQ (5672/15672)            │
│  ┌────────────────────────────────────┐  │
│  │  Exchange: order.exchange (Topic)  │  │
│  └──────────┬─────────────────────────┘  │
│             │                             │
│   RoutingKey: order.notification        │
│             │                             │
│  ┌──────────▼─────────────────────────┐  │
│  │ Queue: order.notification.queue    │  │
│  └──────────┬─────────────────────────┘  │
└─────────────┼──────────────────────────┘
              │
              ▼
┌──────────────────────────────────────────┐
│    NotificationConsumer (order-service)   │
│  ┌────────────────────────────────────┐  │
│  │ 1. 监听order.notification.queue    │  │
│  │ 2. 处理OrderCreatedEvent           │  │
│  │ 3. 发送通知给买家（模拟）          │  │
│  │ 4. 发送通知给卖家（模拟）          │  │
│  │ 5. 手动确认消息（ACK）             │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

---

## 核心组件

### 1. 消息事件类

**位置**: `order-service/src/main/java/com/zjgsu/lll/secondhand/event/OrderCreatedEvent.java`

**作用**: 定义订单创建事件的数据结构

**字段**:
- `orderId`: 订单ID
- `orderNo`: 订单编号
- `buyerId`: 买家ID
- `sellerId`: 卖家ID
- `productId`: 商品ID
- `totalAmount`: 订单总金额
- `shippingAddress`: 配送地址
- `contactPhone`: 联系电话
- `createTime`: 创建时间

### 2. RabbitMQ配置类

**位置**: `order-service/src/main/java/com/zjgsu/lll/secondhand/config/RabbitMQConfig.java`

**作用**: 配置交换机、队列、绑定关系

**核心配置**:

| 组件类型 | 名称 | 类型 | 说明 |
|---------|------|------|------|
| Exchange | `order.exchange` | Topic | 订单主题交换机 |
| Queue | `order.notification.queue` | Durable | 订单通知队列 |
| Binding | - | - | 绑定队列到交换机，路由键：`order.notification` |
| DLX Exchange | `dlx.exchange` | Direct | 死信交换机 |
| DLX Queue | `dlx.queue` | Durable | 死信队列 |

**特性**:
- 消息持久化（Durable）
- JSON消息转换器（Jackson2JsonMessageConverter）
- 发布确认（Publisher Confirms）
- 发布返回（Publisher Returns）
- 死信队列（Dead Letter Exchange）

### 3. 消息生产者

**位置**: `order-service/src/main/java/com/zjgsu/lll/secondhand/producer/OrderProducer.java`

**作用**: 发送订单创建消息到RabbitMQ

**核心方法**:
```java
public void sendOrderCreatedEvent(OrderCreatedEvent event)
```

**消息发送逻辑**:
1. 接收OrderCreatedEvent事件
2. 使用RabbitTemplate发送消息到`order.exchange`
3. 使用路由键`order.notification`
4. 异常捕获，避免影响主流程

### 4. 消息消费者

**位置**: `order-service/src/main/java/com/zjgsu/lll/secondhand/consumer/NotificationConsumer.java`

**作用**: 监听订单通知队列，处理订单创建后的通知逻辑

**核心方法**:
```java
@RabbitListener(queues = RabbitMQConfig.ORDER_NOTIFICATION_QUEUE)
public void handleOrderNotification(OrderCreatedEvent event, Message message, Channel channel)
```

**处理流程**:
1. 监听`order.notification.queue`队列
2. 接收OrderCreatedEvent消息
3. 模拟发送通知给买家
4. 模拟发送通知给卖家
5. 手动确认消息（`channel.basicAck`）
6. 异常处理：失败时拒绝消息并重新入队（`channel.basicNack`）

### 5. OrderService修改

**位置**: `order-service/src/main/java/com/zjgsu/lll/secondhand/service/OrderService.java`

**修改内容**:
1. 注入`OrderProducer`依赖
2. 在`createOrder()`方法中，订单保存成功后：
   - 创建`OrderCreatedEvent`事件对象
   - 调用`orderProducer.sendOrderCreatedEvent(event)`发送消息
   - 使用try-catch包裹，确保消息发送失败不影响订单创建

---

## RabbitMQ配置说明

### application.yml配置

```yaml
spring:
  rabbitmq:
    host: ${SPRING_RABBITMQ_HOST:localhost}
    port: ${SPRING_RABBITMQ_PORT:5672}
    username: ${SPRING_RABBITMQ_USERNAME:admin}
    password: ${SPRING_RABBITMQ_PASSWORD:admin123}
    listener:
      simple:
        acknowledge-mode: manual  # 手动确认模式
        prefetch: 1  # 每次只处理一条消息
        retry:
          enabled: true
          initial-interval: 3000
          max-attempts: 3
          multiplier: 2
    publisher-confirm-type: correlated  # 发布确认
    publisher-returns: true  # 发布返回
```

### Docker环境变量

在`docker-compose.yml`中，order-service的RabbitMQ配置通过环境变量注入：

```yaml
order-service:
  environment:
    - SPRING_RABBITMQ_HOST=rabbitmq
    - SPRING_RABBITMQ_PORT=5672
    - SPRING_RABBITMQ_USERNAME=admin
    - SPRING_RABBITMQ_PASSWORD=admin123
  depends_on:
    rabbitmq:
      condition: service_healthy
```

---

## 消息可靠性保证

### 1. 生产者确认机制

- **Publisher Confirms**: 消息成功发送到交换机后返回确认
- **Publisher Returns**: 消息无法路由到队列时返回

```java
rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
    if (ack) {
        System.out.println("✅ 消息发送成功");
    } else {
        System.err.println("❌ 消息发送失败: " + cause);
    }
});
```

### 2. 消费者确认机制

- **Manual Acknowledgment**: 手动确认模式
- **成功处理**: `channel.basicAck()` 确认消息
- **处理失败**: `channel.basicNack()` 拒绝消息并重新入队

### 3. 死信队列（DLX）

当消息处理失败次数超过阈值时，消息会被发送到死信队列，防止消息丢失：

```java
@Bean
public Queue orderNotificationQueue() {
    return QueueBuilder
            .durable(ORDER_NOTIFICATION_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
            .build();
}
```

### 4. 消息持久化

- 交换机持久化：`durable(true)`
- 队列持久化：`durable(true)`
- 消息持久化：默认启用

---

## 异步消息优势

### 1. 解耦

- 订单服务不需要直接调用通知服务
- 新增消费者无需修改生产者代码
- 服务间依赖降低

### 2. 异步处理

- 订单创建不会被通知逻辑阻塞
- 提高接口响应速度
- 改善用户体验

### 3. 削峰填谷

- 应对高并发场景
- 消息队列缓冲请求
- 消费者按能力处理

### 4. 可靠性

- 消息持久化，防止丢失
- 失败重试机制
- 死信队列兜底

---

## 扩展方向

### 1. 多种通知方式

当前为模拟通知，可扩展为：
- 邮件通知（集成JavaMail）
- 短信通知（集成阿里云SMS）
- 站内信通知（存储到数据库）
- 推送通知（集成推送服务）

### 2. 更多业务事件

可以为其他业务场景创建消息：
- 订单支付成功事件
- 订单发货事件
- 订单完成事件
- 商品上架事件
- 用户注册事件

### 3. 消息监控

- 集成RabbitMQ管理插件
- 实时监控队列长度
- 消息堆积告警
- 消费者性能监控

### 4. 分布式事务

- 集成Seata实现分布式事务
- 保证订单创建与库存扣减的一致性
- 使用TCC或Saga模式

---

## 技术栈

- **RabbitMQ**: 3.13-management
- **Spring AMQP**: 3.1.5
- **Jackson**: JSON消息序列化
- **Spring Boot**: 3.1.5

---

## 相关文档

- [启动指南-阶段6.md](启动指南) - 详细的启动和测试指南
- [当前系统启动指南.md](当前系统启动指南.md) - 系统整体启动指南
- [当前系统README.md](当前系统README.md) - 系统架构文档
