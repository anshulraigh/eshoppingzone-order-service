package com.eshoppingzone.orderservice.dto;

import lombok.Data;

@Data
public class Product {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private Long categoryId;
    private String categoryName;
}
