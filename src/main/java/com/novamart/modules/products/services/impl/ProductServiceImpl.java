package com.novamart.modules.products.services.impl;

import com.novamart.common.exception.NotFoundException;
import com.novamart.modules.products.dto.ProductRequest;
import com.novamart.modules.products.dto.ProductResponse;
import com.novamart.modules.products.entity.Product;
import com.novamart.modules.products.mapper.ProductMapper;
import com.novamart.modules.products.repository.ProductRepository;
import com.novamart.modules.products.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format("Product with id %d not found", id)
                        )
                );

        return productMapper.toDTO(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse addProduct(ProductRequest productRequest) {
        Product product = productRepository.save(productMapper.toEntity(productRequest));

        return productMapper.toDTO(product);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format("Product with id %d not found", id)
                        )
                );

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());

        return productMapper.toDTO(productRepository.save(product));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format("Product with id %d not found", id)
                        )
                );

        productRepository.delete(product);
    }
}
