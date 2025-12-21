package com.zjgsu.lll.secondhand.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjgsu.lll.secondhand.client.ProductClient;
import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.entity.Order;
import com.zjgsu.lll.secondhand.exception.BusinessException;
import com.zjgsu.lll.secondhand.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository, ProductClient productClient, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.objectMapper = objectMapper;
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

        return orderRepository.save(order);
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