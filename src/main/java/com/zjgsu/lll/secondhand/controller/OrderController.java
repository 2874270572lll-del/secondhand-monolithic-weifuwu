package com.zjgsu.lll.secondhand.controller;

import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.entity.Order;
import com.zjgsu.lll.secondhand.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Result<List<Order>> getAllOrders() {
        return Result.success(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    @GetMapping("/orderNo/{orderNo}")
    public Result<Order> getOrderByOrderNo(@PathVariable String orderNo) {
        return Result.success(orderService.getOrderByOrderNo(orderNo));
    }

    @GetMapping("/buyer/{buyerId}")
    public Result<List<Order>> getOrdersByBuyer(@PathVariable Long buyerId) {
        return Result.success(orderService.getOrdersByBuyer(buyerId));
    }

    @GetMapping("/seller/{sellerId}")
    public Result<List<Order>> getOrdersBySeller(@PathVariable Long sellerId) {
        return Result.success(orderService.getOrdersBySeller(sellerId));
    }

    @GetMapping("/status/{status}")
    public Result<List<Order>> getOrdersByStatus(@PathVariable Integer status) {
        return Result.success(orderService.getOrdersByStatus(status));
    }

    @PostMapping
    public Result<Order> createOrder(@Valid @RequestBody Order order) {
        return Result.success(orderService.createOrder(order));
    }

    @PutMapping("/{id}/pay")
    public Result<Order> payOrder(@PathVariable Long id) {
        return Result.success(orderService.payOrder(id));
    }

    @PutMapping("/{id}/ship")
    public Result<Order> shipOrder(@PathVariable Long id) {
        return Result.success(orderService.shipOrder(id));
    }

    @PutMapping("/{id}/finish")
    public Result<Order> finishOrder(@PathVariable Long id) {
        return Result.success(orderService.finishOrder(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success();
    }
}
