package com.eshoppingzone.orderservice.controller;

import com.eshoppingzone.orderservice.model.Order;
import com.eshoppingzone.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Order placeOrder(@RequestBody Order order,
                            @RequestHeader("X-User-Id") Long userId) {
        return orderService.placeOrder(order, userId);
    }

    @GetMapping
    public List<Order> getOrders(@RequestHeader("X-User-Id") Long userId,
                                 @RequestHeader("X-User-Role") String role) {
        return orderService.getOrders(userId, role);
    }

    @GetMapping("/{id}")
    public Optional<Order> getOrderById(@PathVariable Long id,
                                        @RequestHeader("X-User-Id") Long userId,
                                        @RequestHeader("X-User-Role") String role) {
        return orderService.getOrderById(id, userId, role);
    }

    @PutMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id,
                                   @RequestParam String status,
                                   @RequestHeader("X-User-Id") Long userId,
                                   @RequestHeader("X-User-Role") String role) {
        return orderService.updateOrderStatus(id, status, userId, role);
    }

    @DeleteMapping("/{id}")
    public void cancelOrder(@PathVariable Long id,
                            @RequestHeader("X-User-Id") Long userId,
                            @RequestHeader("X-User-Role") String role) {
        orderService.cancelOrder(id, userId, role);
    }
}
