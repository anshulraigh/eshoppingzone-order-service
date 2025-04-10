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
        System.err.println("Fallback: Unable to update product");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Product service is unavailable for updating product");
    }

    @Override
    public boolean reduceProductQuantity(Long id, int quantity) {
        System.err.println("Fallback: Unable to reduce product quantity");
        return false;
    }

    @Override
    public void restoreProductQuantity(Long id, int quantity) {
        System.err.println("Fallback: Unable to restore product quantity");
    }
}
