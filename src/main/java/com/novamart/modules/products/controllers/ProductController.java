package com.novamart.modules.products.controllers;

import com.novamart.common.response.ApiResponse;
import com.novamart.constants.MessageConstants;
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
        List<ProductResponse> products = productService.getAllProducts();

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), MessageConstants.LIST_PRODUCT_FOUNDED, products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), MessageConstants.PRODUCT_FOUNDED, product));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(@Valid @RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.addProduct(productRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), MessageConstants.PRODUCT_CREATED, productResponse));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest productRequest) {
        ProductResponse productResponseUpdated = productService.updateProduct(id, productRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), MessageConstants.PRODUCT_UPDATED, productResponseUpdated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), MessageConstants.PRODUCT_DELETED, null));
    }
}
