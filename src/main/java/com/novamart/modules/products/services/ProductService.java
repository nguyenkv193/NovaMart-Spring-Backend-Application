package com.novamart.modules.products.services;

import com.novamart.modules.products.dto.ProductRequest;
import com.novamart.modules.products.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse getProductById(Long id);
    List<ProductResponse> getAllProducts();

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    void deleteProduct(Long id);
}
