package com.eshoppingzone.orderservice.client;

import com.eshoppingzone.orderservice.dto.PaymentRequest;
import com.eshoppingzone.orderservice.dto.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentClientFallback implements PaymentClient {

    @Override
    public PaymentResponse makePayment(PaymentRequest paymentRequest) {
        System.err.println("Fallback: Payment service unavailable");
        return new PaymentResponse("FAILED", "Payment service is down", null);
    }

    @Override
    public PaymentResponse getPaymentStatus(Long orderId) {
        System.err.println("Fallback: Payment status unavailable");
        return new PaymentResponse("UNKNOWN", "Payment status not available", null);
    }
}