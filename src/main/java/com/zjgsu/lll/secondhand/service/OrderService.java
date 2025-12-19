package com.zjgsu.lll.secondhand.service;

import com.zjgsu.lll.secondhand.entity.Order;
import com.zjgsu.lll.secondhand.entity.Product;
import com.zjgsu.lll.secondhand.exception.BusinessException;
import com.zjgsu.lll.secondhand.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
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
        Product product = productService.getProductById(order.getProductId());

        if (product.getStatus() != 1) {
            throw new BusinessException("Product is not available");
        }

        if (product.getStock() < 1) {
            throw new BusinessException("Product is out of stock");
        }

        order.setOrderNo(generateOrderNo());
        order.setSellerId(product.getSellerId());
        order.setTotalAmount(product.getPrice());
        order.setStatus(0);

        return orderRepository.save(order);
    }

    @Transactional
    public Order payOrder(Long id) {
        Order order = getOrderById(id);

        if (order.getStatus() != 0) {
            throw new BusinessException("Order cannot be paid");
        }

        productService.reduceStock(order.getProductId(), 1);

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
