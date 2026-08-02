package com.novamart.modules.products.dto;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private Long quantity;
}
