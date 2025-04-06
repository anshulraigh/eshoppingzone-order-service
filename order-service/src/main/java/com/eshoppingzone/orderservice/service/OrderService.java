package com.eshoppingzone.orderservice.service;

import com.eshoppingzone.orderservice.client.UserClient;
import com.eshoppingzone.orderservice.model.Order;
import com.eshoppingzone.orderservice.model.User;
import com.eshoppingzone.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserClient userClient;

    public Order placeOrder(Order order) {
        // Validate user existence
        User user = userClient.getUserById(order.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        order.setStatus("PLACED");
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        } else {
            throw new RuntimeException("Order not found");
        }
    }

    public void cancelOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }
}
