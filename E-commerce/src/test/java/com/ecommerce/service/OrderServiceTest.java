package com.ecommerce.service;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartService cartService;
    @InjectMocks private OrderService orderService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("Test", "test@example.com", "enc"); testUser.setId(1L);
        testProduct = new Product("Laptop", "Gaming", new BigDecimal("999.99"), 10, null); testProduct.setId(1L);
        testCart = new Cart(testUser); testCart.setId(1L);
        testCart.setItems(new ArrayList<>(List.of(new CartItem(testCart, testProduct, 2))));
        orderRequest = new OrderRequest();
        orderRequest.setShippingAddress("123 Main St"); orderRequest.setShippingCity("Springfield");
        orderRequest.setShippingState("IL"); orderRequest.setShippingZipCode("62701"); orderRequest.setShippingCountry("US");
    }

    @Test void createOrder_ValidCart_ShouldCreate() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.getCartForUser("test@example.com")).thenReturn(testCart);
        when(productRepository.save(any())).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> { Order o = i.getArgument(0); o.setId(1L); return o; });
        OrderResponse r = orderService.createOrderFromCart("test@example.com", orderRequest);
        assertNotNull(r); assertEquals("PENDING", r.getOrderStatus());
        verify(cartService).clearCart("test@example.com");
    }
    @Test void createOrder_EmptyCart_ShouldThrow() {
        Cart empty = new Cart(testUser); empty.setItems(new ArrayList<>());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.getCartForUser("test@example.com")).thenReturn(empty);
        assertThrows(BadRequestException.class, () -> orderService.createOrderFromCart("test@example.com", orderRequest));
    }
    @Test void createOrder_InsufficientStock_ShouldThrow() {
        testProduct.setStockQuantity(1);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.getCartForUser("test@example.com")).thenReturn(testCart);
        assertThrows(BadRequestException.class, () -> orderService.createOrderFromCart("test@example.com", orderRequest));
    }
    @Test void getUserOrders_ShouldReturnList() {
        Order o = new Order(); o.setId(1L); o.setUser(testUser); o.setTotalPrice(new BigDecimal("1999.98"));
        o.setShippingAddress("123 Main St"); o.setItems(new ArrayList<>());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(o));
        assertEquals(1, orderService.getUserOrders("test@example.com").size());
    }
    @Test void getOrderById_NotFound_ShouldThrow() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById("test@example.com", 999L));
    }
    @Test void cancelOrder_Pending_ShouldCancel() {
        Order o = new Order(); o.setId(1L); o.setUser(testUser); o.setOrderStatus(OrderStatus.PENDING);
        o.setTotalPrice(new BigDecimal("1999.98")); o.setShippingAddress("123 Main St");
        o.setItems(new ArrayList<>(List.of(new OrderItem(o, testProduct, 2, testProduct.getPrice()))));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(o));
        when(productRepository.save(any())).thenReturn(testProduct);
        when(orderRepository.save(any())).thenReturn(o);
        assertEquals("CANCELLED", orderService.cancelOrder("test@example.com", 1L).getOrderStatus());
    }
    @Test void cancelOrder_NonPending_ShouldThrow() {
        Order o = new Order(); o.setId(1L); o.setUser(testUser); o.setOrderStatus(OrderStatus.CONFIRMED);
        o.setTotalPrice(new BigDecimal("100")); o.setShippingAddress("x"); o.setItems(new ArrayList<>());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(o));
        assertThrows(BadRequestException.class, () -> orderService.cancelOrder("test@example.com", 1L));
    }
}
