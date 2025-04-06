package com.eshoppingzone.orderservice.service;

import com.eshoppingzone.orderservice.client.ProductClient;
import com.eshoppingzone.orderservice.dto.Product;
import com.eshoppingzone.orderservice.model.Order;
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
    private ProductClient productClient;

    public Order placeOrder(Order order, Long userId) {
        Product product = productClient.getProductById(order.getProductId());

        if (product == null || product.getQuantity() < order.getQuantity()) {
            throw new RuntimeException("Product unavailable or insufficient stock");
        }

        order.setUserId(userId);
        order.setProductName(product.getName());
        order.setPrice(product.getPrice());
        order.setStatus("PLACED");

        product.setQuantity(product.getQuantity() - order.getQuantity());
        productClient.updateProduct(product.getId(), product);

        return orderRepository.save(order);
    }

    public List<Order> getOrders(Long userId, String role) {
        return "ADMIN".equalsIgnoreCase(role)
                ? orderRepository.findAll()
                : orderRepository.findByUserId(userId);
    }

    public Optional<Order> getOrderById(Long orderId, Long userId, String role) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            if ("ADMIN".equalsIgnoreCase(role) || order.get().getUserId().equals(userId)) {
                return order;
            } else {
                throw new RuntimeException("Unauthorized access");
            }
        }
        return Optional.empty();
    }

    public Order updateOrderStatus(Long orderId, String status, Long userId, String role) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if ("ADMIN".equalsIgnoreCase(role) || order.getUserId().equals(userId)) {
                order.setStatus(status);
                return orderRepository.save(order);
            } else {
                throw new RuntimeException("Unauthorized to update this order");
            }
        } else {
            throw new RuntimeException("Order not found");
        }
    }

    public void cancelOrder(Long orderId, Long userId, String role) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if ("ADMIN".equalsIgnoreCase(role) || order.getUserId().equals(userId)) {
                orderRepository.deleteById(orderId);
            } else {
                throw new RuntimeException("Unauthorized to cancel this order");
            }
        } else {
            throw new RuntimeException("Order not found");
        }
    }
}
