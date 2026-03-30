package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;
    private Product testProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product("Laptop", "Gaming laptop", new BigDecimal("999.99"), 10, "http://img.url");
        testProduct.setId(1L);
        productRequest = new ProductRequest();
        productRequest.setName("Laptop"); productRequest.setDescription("Gaming laptop");
        productRequest.setPrice(new BigDecimal("999.99")); productRequest.setStockQuantity(10);
    }

    @Test void createProduct_ShouldReturnSaved() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        Product result = productService.createProduct(productRequest);
        assertEquals("Laptop", result.getName());
    }
    @Test void getAllProducts_ShouldReturnList() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));
        assertEquals(1, productService.getAllProducts().size());
    }
    @Test void getProductById_Existing_ShouldReturn() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        assertNotNull(productService.getProductById(1L));
    }
    @Test void getProductById_NonExisting_ShouldThrow() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }
    @Test void updateProduct_ShouldUpdateFields() {
        productRequest.setName("Updated"); productRequest.setPrice(new BigDecimal("1099.99"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any())).thenReturn(testProduct);
        productService.updateProduct(1L, productRequest);
        assertEquals("Updated", testProduct.getName());
    }
    @Test void deleteProduct_ShouldDelete() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        productService.deleteProduct(1L);
        verify(productRepository).delete(testProduct);
    }
    @Test void searchProducts_ShouldReturnMatching() {
        when(productRepository.findByNameContainingIgnoreCase("lap")).thenReturn(List.of(testProduct));
        assertEquals(1, productService.searchProducts("lap").size());
    }
}
