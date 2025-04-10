package com.eshoppingzone.orderservice.service;

import com.eshoppingzone.orderservice.client.ProductClient;
import com.eshoppingzone.orderservice.config.UserContext;
import com.eshoppingzone.orderservice.dto.Product;
import com.eshoppingzone.orderservice.model.Order;
import com.eshoppingzone.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private ProductClient productClient;
    private OrderService orderService;

    private final Long userId = 101L;
    private final String userRole = "USER";
    private final String adminRole = "ADMIN";

    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productClient = mock(ProductClient.class);
        orderService = new OrderService();
        orderService.orderRepository = orderRepository;
        orderService.productClient = productClient;

        order = new Order();
        order.setProductId(1L);
        order.setQuantity(2);

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setQuantity(10);
    }

    @Test
    void placeOrder_success() {
        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(userId);

            when(productClient.getProductById(order.getProductId())).thenReturn(product);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Order result = orderService.placeOrder(order);

            assertNotNull(result);
            assertEquals("PENDING", result.getStatus());
            verify(productClient).reduceProductQuantity(product.getId(), order.getQuantity());
        }
    }

    @Test
    void placeOrder_insufficientStock_throwsException() {
        product.setQuantity(1); // Not enough stock

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(userId);

            when(productClient.getProductById(order.getProductId())).thenReturn(product);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.placeOrder(order));
            assertTrue(ex.getMessage().contains("Insufficient stock"));
        }
    }

    @Test
    void getOrders_asAdmin_returnsAllOrders() {
        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(userId);
            mockedUserContext.when(UserContext::getRole).thenReturn(adminRole);

            List<Order> expectedOrders = Arrays.asList(new Order(), new Order());
            when(orderRepository.findAll()).thenReturn(expectedOrders);

            List<Order> result = orderService.getOrders();

            assertEquals(2, result.size());
        }
    }

    @Test
    void getOrders_asUser_returnsOwnOrders() {
        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(userId);
            mockedUserContext.when(UserContext::getRole).thenReturn(userRole);

            List<Order> expectedOrders = Arrays.asList(new Order(), new Order());
            when(orderRepository.findByUserId(userId)).thenReturn(expectedOrders);

            List<Order> result = orderService.getOrders();

            assertEquals(2, result.size());
        }
    }

    @Test
    void getOrderById_ownerAccess_success() {
        Order savedOrder = new Order();
        savedOrder.setUserId(userId);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(userId);
            mockedUserContext.when(UserContext::getRole).thenReturn(userRole);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

            Optional<Order> result = orderService.getOrderById(1L);

            assertTrue(result.isPresent());
        }
    }

    @Test
    void updateOrderStatus_adminAccess_success() {
        Order savedOrder = new Order();
        savedOrder.setUserId(userId);
        savedOrder.setStatus("PENDING");

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(userId);
            mockedUserContext.when(UserContext::getRole).thenReturn(adminRole);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            Order updated = orderService.updateOrderStatus(1L, "SHIPPED");

            assertEquals("SHIPPED", updated.getStatus());
        }
    }

    @Test
    void cancelOrder_ownerAccess_success() {
        Order savedOrder = new Order();
        savedOrder.setUserId(userId);

        try (MockedStatic<UserContext> mockedUserContext = Mockito.mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn(userId);
            mockedUserContext.when(UserContext::getRole).thenReturn(userRole);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

            orderService.cancelOrder(1L);

            verify(orderRepository).deleteById(1L);
        }
    }
}
