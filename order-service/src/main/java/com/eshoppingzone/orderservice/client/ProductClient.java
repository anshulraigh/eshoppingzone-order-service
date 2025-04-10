package com.eshoppingzone.orderservice.client;

import com.eshoppingzone.orderservice.dto.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/products/{id}")
    Product getProductById(@PathVariable("id") Long id);

    @PutMapping("/products/{id}")
    ResponseEntity<?> updateProduct(@PathVariable("id") Long id, @RequestBody Product product);

    @PutMapping("/products/{id}/reduce-stock")
    boolean reduceProductQuantity(@PathVariable Long id, @RequestParam int quantity);

    @PutMapping("/products/{id}/restore-stock")
    void restoreProductQuantity(@PathVariable Long id, @RequestParam int quantity);

}
