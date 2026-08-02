package com.novamart.modules.products.controllers;

import com.novamart.common.response.ApiResponse;
import com.novamart.modules.products.constants.ProductMessageConstants;
import com.novamart.modules.products.dto.ProductRequest;
import com.novamart.modules.products.dto.ProductResponse;
import com.novamart.modules.products.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        final List<ProductResponse> products = productService.getAllProducts();

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                ProductMessageConstants.PRODUCTS_FETCHED_SUCCESSFULLY,
                products
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable("id") Long id) {
        final ProductResponse product = productService.getProductById(id);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                ProductMessageConstants.PRODUCT_FOUND_SUCCESSFULLY,
                product
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest productRequest
    ) {
        final ProductResponse productResponse = productService.createProduct(productRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        ProductMessageConstants.PRODUCT_CREATED_SUCCESSFULLY,
                        productResponse
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable("id") Long id,
            @Valid @RequestBody ProductRequest productRequest
    ) {
        final ProductResponse productResponse = productService.updateProduct(id, productRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        ProductMessageConstants.PRODUCT_UPDATED_SUCCESSFULLY,
                        productResponse
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        ProductMessageConstants.PRODUCT_DELETED_SUCCESSFULLY,
                        null
                ));
    }
}
