package com.eshoppingzone.orderservice.service;

import com.eshoppingzone.orderservice.client.PaymentClient;
import com.eshoppingzone.orderservice.client.ProductClient;
import com.eshoppingzone.orderservice.config.UserContext;
import com.eshoppingzone.orderservice.dto.PaymentRequest;
import com.eshoppingzone.orderservice.dto.PaymentResponse;
import com.eshoppingzone.orderservice.dto.Product;
import com.eshoppingzone.orderservice.model.Order;
import com.eshoppingzone.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductClient productClient;

    @Autowired
    PaymentClient paymentClient;

    @Transactional
    public Order placeOrder(Order order) {
        Long userId = UserContext.getUserId();
        Product product = productClient.getProductById(order.getProductId());
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        if (product.getQuantity() < order.getQuantity()) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        try {
            productClient.reduceProductQuantity(product.getId(), order.getQuantity());

            order.setUserId(userId);
            order.setProductName(product.getName());
            order.setPrice(product.getPrice());
            order.setStatus("PENDING");

            Order savedOrder = orderRepository.save(order);

            PaymentRequest paymentRequest = new PaymentRequest(
                    savedOrder.getId(),
                    userId,
                    savedOrder.getPrice() * savedOrder.getQuantity(),
                    "card"
            );

            PaymentResponse paymentResponse = paymentClient.makePayment(paymentRequest);

            if (!"PENDING".equalsIgnoreCase(paymentResponse.getStatus())) {
                productClient.restoreProductQuantity(product.getId(), order.getQuantity());
                throw new RuntimeException("Payment initiation failed: " + paymentResponse.getMessage());
            }

            savedOrder.setPaymentLink(paymentResponse.getPaymentLink());
            Order updatedOrder = orderRepository.save(savedOrder);

            verifyAndUpdatePaymentStatus(updatedOrder);

            return updatedOrder;

        } catch (Exception e) {
            if (order.getId() != null) {
                productClient.restoreProductQuantity(product.getId(), order.getQuantity());
            }
            throw new RuntimeException("Order placement failed: " + e.getMessage());
        }
    }

    public List<Order> getOrders() {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();

        if (isAdmin(role)) {
            return orderRepository.findAll();
        }
        return orderRepository.findByUserId(userId);
    }

    public Optional<Order> getOrderById(Long orderId) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (isAdmin(role) || isOwner(order, userId)) {
                if ("PENDING".equalsIgnoreCase(order.getStatus())) {
                    verifyAndUpdatePaymentStatus(order);
                }
                return Optional.of(order);
            } else {
                throw new RuntimeException("Unauthorized access to order");
            }
        }
        return Optional.empty();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (isAdmin(role) || isOwner(order, userId)) {
            order.setStatus(status);
            return orderRepository.save(order);
        } else {
            throw new RuntimeException("Unauthorized to update this order");
        }
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Long userId = UserContext.getUserId();
        String role = UserContext.getRole();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (isAdmin(role) || isOwner(order, userId)) {
            if ("PENDING".equalsIgnoreCase(order.getStatus())) {
                productClient.restoreProductQuantity(order.getProductId(), order.getQuantity());
            }
            orderRepository.deleteById(orderId);
        } else {
            throw new RuntimeException("Unauthorized to cancel this order");
        }
    }

    private void verifyAndUpdatePaymentStatus(Order order) {
        try {
            log.info("Verifying payment status for order: {}", order.getId());
            PaymentResponse paymentStatus = paymentClient.getPaymentStatus(order.getId());
            if ("SUCCESS".equalsIgnoreCase(paymentStatus.getStatus())) {
                log.info("Updating order status to PLACED for order: {}", order.getId());
                order.setStatus("PLACED");
                orderRepository.save(order);
            }
        } catch (Exception e) {
            log.error("Failed to verify payment status for order: {}", order.getId(), e);
        }
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    private boolean isOwner(Order order, Long userId) {
        return order.getUserId().equals(userId);
    }
}
