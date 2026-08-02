package com.novamart.modules.products.services.impl;

import com.novamart.common.exception.NotFoundException;
import com.novamart.modules.products.constants.ProductMessageConstants;
import com.novamart.modules.products.dto.ProductRequest;
import com.novamart.modules.products.dto.ProductResponse;
import com.novamart.modules.products.entity.Product;
import com.novamart.modules.products.mapper.ProductMapper;
import com.novamart.modules.products.repository.ProductRepository;
import com.novamart.modules.products.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productMapper.toResponse(findProductById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        final List<Product> products = productRepository.findAll();

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        final Product product = productMapper.toEntity(productRequest);
        final Product savedProduct = productRepository.save(product);
        log.info("Product created with id={}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        final Product product = findProductById(id);

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());

        final Product updatedProduct = productRepository.save(product);
        log.info("Product updated with id={}", updatedProduct.getId());

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteProduct(Long id) {
        final Product product = findProductById(id);
        productRepository.delete(product);
        log.info("Product deleted with id={}", id);
    }

    private Product findProductById(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundException(
                                String.format(ProductMessageConstants.PRODUCT_NOT_FOUND, id)
                        )
                );
    }
}
