package com.eshoppingzone.orderservice.client;

import com.eshoppingzone.orderservice.dto.PaymentRequest;
import com.eshoppingzone.orderservice.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @PostMapping("/payments")
    PaymentResponse makePayment(@RequestBody PaymentRequest paymentRequest);

    @GetMapping("/payments/{orderId}")
    PaymentResponse getPaymentStatus(@PathVariable Long orderId);
}