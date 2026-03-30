package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductService productService;
    @InjectMocks private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = new User("Test", "test@example.com", "enc"); testUser.setId(1L);
        testCart = new Cart(testUser); testCart.setId(1L); testCart.setItems(new ArrayList<>());
        testProduct = new Product("Item", "Desc", new BigDecimal("29.99"), 100, null); testProduct.setId(1L);
    }

    @Test void getCart_ShouldReturnResponse() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        CartResponse r = cartService.getCart("test@example.com");
        assertNotNull(r); assertTrue(r.getItems().isEmpty());
    }
    @Test void addItem_ValidProduct_ShouldAdd() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(cartItemRepository.findByCartIdAndProductId(1L,1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        assertNotNull(cartService.addItemToCart("test@example.com", new CartItemRequest(1L,2)));
    }
    @Test void addItem_InsufficientStock_ShouldThrow() {
        testProduct.setStockQuantity(1);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productService.getProductById(1L)).thenReturn(testProduct);
        assertThrows(BadRequestException.class, () -> cartService.addItemToCart("test@example.com", new CartItemRequest(1L,200)));
    }
    @Test void addItem_Existing_ShouldUpdateQty() {
        CartItem existing = new CartItem(testCart, testProduct, 2); existing.setId(1L);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(cartItemRepository.findByCartIdAndProductId(1L,1L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        cartService.addItemToCart("test@example.com", new CartItemRequest(1L,3));
        assertEquals(5, existing.getQuantity());
    }
    @Test void removeItem_NonExistent_ShouldThrow() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(1L,999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItemFromCart("test@example.com", 999L));
    }
    @Test void clearCart_ShouldRemoveAll() {
        testCart.getItems().add(new CartItem(testCart, testProduct, 1));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any())).thenReturn(testCart);
        cartService.clearCart("test@example.com");
        assertTrue(testCart.getItems().isEmpty());
    }
}
