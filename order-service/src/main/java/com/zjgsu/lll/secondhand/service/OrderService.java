package com.zjgsu.lll.secondhand.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjgsu.lll.secondhand.client.ProductClient;
import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.entity.Order;
import com.zjgsu.lll.secondhand.event.OrderCreatedEvent;
import com.zjgsu.lll.secondhand.exception.BusinessException;
import com.zjgsu.lll.secondhand.producer.OrderProducer;
import com.zjgsu.lll.secondhand.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final ObjectMapper objectMapper;
    private final OrderProducer orderProducer;

    public OrderService(OrderRepository orderRepository, ProductClient productClient,
                       ObjectMapper objectMapper, OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.objectMapper = objectMapper;
        this.orderProducer = orderProducer;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Order not found"));
    }

    public Order getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException("Order not found"));
    }

    public List<Order> getOrdersByBuyer(Long buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }

    public List<Order> getOrdersBySeller(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    public List<Order> getOrdersByStatus(Integer status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order createOrder(Order order) {
        // 调用商品服务获取商品详情
        Result<?> productResult = productClient.getProductById(order.getProductId());

        if (productResult.getCode() == 503) {
            throw new BusinessException("商品服务暂时不可用,请稍后重试");
        }

        if (productResult.getCode() != 200 || productResult.getData() == null) {
            throw new BusinessException("Product not found");
        }

        try {
            // 从商品数据中提取 sellerId
            Map<String, Object> productData = objectMapper.convertValue(
                    productResult.getData(),
                    Map.class
            );

            Long sellerId = ((Number) productData.get("sellerId")).longValue();
            order.setSellerId(sellerId);

            // 验证库存（可选）
            Integer stock = ((Number) productData.get("stock")).intValue();
            if (stock <= 0) {
                throw new BusinessException("Product out of stock");
            }

        } catch (Exception e) {
            throw new BusinessException("Failed to parse product data: " + e.getMessage());
        }

        // 生成订单号并设置初始状态
        order.setOrderNo(generateOrderNo());
        order.setStatus(0); // 0-待支付

        // 保存订单
        Order savedOrder = orderRepository.save(order);

        // 发送订单创建消息到RabbitMQ（异步通知）
        try {
            OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getOrderNo(),
                savedOrder.getBuyerId(),
                savedOrder.getSellerId(),
                savedOrder.getProductId(),
                savedOrder.getTotalAmount(),
                savedOrder.getShippingAddress(),
                savedOrder.getContactPhone(),
                savedOrder.getCreateTime()
            );
            orderProducer.sendOrderCreatedEvent(event);
        } catch (Exception e) {
            // 消息发送失败不影响订单创建
            log.error("发送订单创建消息失败，但订单已创建成功: orderId={}, error={}",
                    savedOrder.getId(), e.getMessage());
        }

        return savedOrder;
    }

    @Transactional
    public Order payOrder(Long id) {
        Order order = getOrderById(id);

        if (order.getStatus() != 0) {
            throw new BusinessException("Order cannot be paid");
        }

        // 调用商品服务减少库存
        Result<?> result = productClient.reduceStock(order.getProductId(), 1);

        if (result.getCode() != 200) {
            throw new BusinessException("Failed to reduce product stock");
        }

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order shipOrder(Long id) {
        Order order = getOrderById(id);

        if (order.getStatus() != 1) {
            throw new BusinessException("Order cannot be shipped");
        }

        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order finishOrder(Long id) {
        Order order = getOrderById(id);

        if (order.getStatus() != 2) {
            throw new BusinessException("Order cannot be finished");
        }

        order.setStatus(3);
        order.setFinishTime(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = getOrderById(id);

        if (order.getStatus() != 0) {
            throw new BusinessException("Order cannot be cancelled");
        }

        order.setStatus(4);
        orderRepository.save(order);
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}