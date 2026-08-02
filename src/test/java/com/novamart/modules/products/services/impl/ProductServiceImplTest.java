package com.novamart.modules.products.services.impl;

import com.novamart.common.exception.NotFoundException;
import com.novamart.modules.products.dto.ProductRequest;
import com.novamart.modules.products.dto.ProductResponse;
import com.novamart.modules.products.entity.Product;
import com.novamart.modules.products.mapper.ProductMapper;
import com.novamart.modules.products.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldReturnProductWhenProductExists() {
        final Product product = product(1L);
        final ProductResponse response = response(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        final ProductResponse result = productService.getProductById(1L);

        assertSame(response, result);
        verify(productMapper).toResponse(product);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void shouldReturnAllProductsWhenProductsExist() {
        final Product firstProduct = product(1L);
        final Product secondProduct = product(2L);
        final ProductResponse firstResponse = response(1L);
        final ProductResponse secondResponse = response(2L);
        when(productRepository.findAll()).thenReturn(List.of(firstProduct, secondProduct));
        when(productMapper.toResponse(firstProduct)).thenReturn(firstResponse);
        when(productMapper.toResponse(secondProduct)).thenReturn(secondResponse);

        final List<ProductResponse> result = productService.getAllProducts();

        assertEquals(List.of(firstResponse, secondResponse), result);
    }

    @Test
    void shouldCreateProductWhenRequestIsValid() {
        final ProductRequest request = request();
        final Product product = product(1L);
        final ProductResponse response = response(1L);
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        final ProductResponse result = productService.createProduct(request);

        assertSame(response, result);
        verify(productRepository).save(product);
    }

    @Test
    void shouldUpdateProductWhenProductExists() {
        final Product product = product(1L);
        final ProductRequest request = new ProductRequest(
                "Updated keyboard",
                "Updated description",
                BigDecimal.valueOf(149.99),
                10L
        );
        final ProductResponse response = response(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        final ProductResponse result = productService.updateProduct(1L, request);

        assertSame(response, result);
        assertEquals(request.getName(), product.getName());
        assertEquals(request.getDescription(), product.getDescription());
        assertEquals(request.getPrice(), product.getPrice());
        assertEquals(request.getQuantity(), product.getQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void shouldDeleteProductWhenProductExists() {
        final Product product = product(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    private static ProductRequest request() {
        return new ProductRequest(
                "Mechanical keyboard",
                "Wireless mechanical keyboard",
                BigDecimal.valueOf(129.99),
                5L
        );
    }

    private static Product product(Long id) {
        final Product product = new Product();
        product.setId(id);
        product.setName("Mechanical keyboard");
        product.setDescription("Wireless mechanical keyboard");
        product.setPrice(BigDecimal.valueOf(129.99));
        product.setQuantity(5L);
        return product;
    }

    private static ProductResponse response(Long id) {
        return new ProductResponse(
                id,
                "Mechanical keyboard",
                "Wireless mechanical keyboard",
                BigDecimal.valueOf(129.99),
                5L
        );
    }
}
