package com.zjgsu.lll.secondhand.consumer;

import com.rabbitmq.client.Channel;
import com.zjgsu.lll.secondhand.config.RabbitMQConfig;
import com.zjgsu.lll.secondhand.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 通知消息消费者
 * 监听订单通知队列，处理订单创建后的通知逻辑
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    /**
     * 处理订单创建通知
     * 向买家和卖家发送通知（这里模拟通知逻辑）
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_NOTIFICATION_QUEUE)
    public void handleOrderNotification(OrderCreatedEvent event, Message message, Channel channel) {
        try {
            log.info("📨 收到订单通知消息: orderNo={}, orderId={}",
                    event.getOrderNo(), event.getOrderId());

            // 模拟发送通知给买家
            sendNotificationToBuyer(event);

            // 模拟发送通知给卖家
            sendNotificationToSeller(event);

            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            log.info("✅ 订单通知处理成功: orderNo={}", event.getOrderNo());

        } catch (Exception e) {
            log.error("❌ 处理订单通知失败: orderNo={}, error={}",
                    event.getOrderNo(), e.getMessage(), e);

            try {
                // 拒绝消息并重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (IOException ioException) {
                log.error("❌ 消息Nack失败: {}", ioException.getMessage());
            }
        }
    }

    /**
     * 发送通知给买家
     */
    private void sendNotificationToBuyer(OrderCreatedEvent event) {
        log.info("📧 [模拟] 向买家发送通知: buyerId={}, orderNo={}, totalAmount={}",
                event.getBuyerId(), event.getOrderNo(), event.getTotalAmount());

        // 这里可以集成邮件服务、短信服务、站内信等
        // 示例：emailService.sendOrderConfirmation(event.getBuyerId(), event);
    }

    /**
     * 发送通知给卖家
     */
    private void sendNotificationToSeller(OrderCreatedEvent event) {
        log.info("📧 [模拟] 向卖家发送通知: sellerId={}, orderNo={}, productId={}",
                event.getSellerId(), event.getOrderNo(), event.getProductId());

        // 这里可以集成邮件服务、短信服务、站内信等
        // 示例：emailService.sendNewOrderNotification(event.getSellerId(), event);
    }
}
