package com.zjgsu.lll.secondhand.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 定义交换机、队列、绑定关系
 */
@Configuration
public class RabbitMQConfig {

    // 交换机名称
    public static final String ORDER_EXCHANGE = "order.exchange";

    // 队列名称
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_NOTIFICATION_QUEUE = "order.notification.queue";

    // 路由键
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_NOTIFICATION_ROUTING_KEY = "order.notification";

    // 死信交换机和队列
    public static final String DLX_EXCHANGE = "dlx.exchange";
    public static final String DLX_QUEUE = "dlx.queue";
    public static final String DLX_ROUTING_KEY = "dlx";

    /**
     * 订单交换机（Topic类型）
     */
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder
                .topicExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 订单创建队列
     */
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder
                .durable(ORDER_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .build();
    }

    /**
     * 订单通知队列
     */
    @Bean
    public Queue orderNotificationQueue() {
        return QueueBuilder
                .durable(ORDER_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定：订单创建队列 -> 订单交换机
     */
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    /**
     * 绑定：订单通知队列 -> 订单交换机
     */
    @Bean
    public Binding orderNotificationBinding() {
        return BindingBuilder
                .bind(orderNotificationQueue())
                .to(orderExchange())
                .with(ORDER_NOTIFICATION_ROUTING_KEY);
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder
                .directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder
                .durable(DLX_QUEUE)
                .build();
    }

    /**
     * 绑定：死信队列 -> 死信交换机
     */
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder
                .bind(dlxQueue())
                .to(dlxExchange())
                .with(DLX_ROUTING_KEY);
    }

    /**
     * 消息转换器（JSON）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate配置
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                        MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        // 开启发送确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("✅ 消息发送成功: " + correlationData);
            } else {
                System.err.println("❌ 消息发送失败: " + correlationData + ", 原因: " + cause);
            }
        });

        // 开启返回确认
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("❌ 消息未路由到队列: " + returned.getMessage());
        });

        return rabbitTemplate;
    }
}
