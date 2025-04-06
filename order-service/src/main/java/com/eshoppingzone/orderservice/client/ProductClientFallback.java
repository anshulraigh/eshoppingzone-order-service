package com.eshoppingzone.orderservice.client;

import com.eshoppingzone.orderservice.dto.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public Product getProductById(Long id) {
        System.err.println("Fallback: Product service unavailable");
        return null;
    }

    @Override
    public ResponseEntity<?> updateProduct(Long id, Product product) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Product service is unavailable");
    }
}
