package com.zjgsu.lll.secondhand.producer;

import com.zjgsu.lll.secondhand.config.RabbitMQConfig;
import com.zjgsu.lll.secondhand.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单消息生产者
 * 负责发送订单相关的消息到RabbitMQ
 */
@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public OrderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送订单创建事件
     * @param event 订单创建事件
     */
    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            log.info("📤 准备发送订单创建消息: {}", event.getOrderNo());

            // 发送到订单通知队列（用于发送通知给买家和卖家）
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_NOTIFICATION_ROUTING_KEY,
                event
            );

            log.info("✅ 订单创建消息发送成功: orderId={}, orderNo={}",
                    event.getOrderId(), event.getOrderNo());

        } catch (Exception e) {
            log.error("❌ 发送订单创建消息失败: orderId={}, error={}",
                    event.getOrderId(), e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }
}
