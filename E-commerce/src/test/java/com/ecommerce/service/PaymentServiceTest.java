package com.ecommerce.service;

import com.ecommerce.config.StripeConfig;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock private OrderService orderService;
    @Mock private OrderRepository orderRepository;
    @Mock private StripeConfig stripeConfig;
    @InjectMocks private PaymentService paymentService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        User u = new User("Test", "test@example.com", "enc"); u.setId(1L);
        testOrder = new Order(); testOrder.setId(1L); testOrder.setUser(u);
        testOrder.setTotalPrice(new BigDecimal("99.99")); testOrder.setShippingAddress("123 Main");
        testOrder.setItems(new ArrayList<>());
    }

    @Test void createIntent_StripeNotConfigured_ShouldThrow() {
        when(stripeConfig.isStripeConfigured()).thenReturn(false);
        assertThrows(BadRequestException.class, () -> paymentService.createPaymentIntent("test@example.com", 1L));
    }
    @Test void createIntent_AlreadyPaid_ShouldThrow() {
        when(stripeConfig.isStripeConfigured()).thenReturn(true);
        testOrder.setPaymentStatus(PaymentStatus.COMPLETED);
        when(orderService.getOrderEntityById("test@example.com", 1L)).thenReturn(testOrder);
        assertThrows(BadRequestException.class, () -> paymentService.createPaymentIntent("test@example.com", 1L));
    }
    @Test void createIntent_CancelledOrder_ShouldThrow() {
        when(stripeConfig.isStripeConfigured()).thenReturn(true);
        testOrder.setOrderStatus(OrderStatus.CANCELLED);
        when(orderService.getOrderEntityById("test@example.com", 1L)).thenReturn(testOrder);
        assertThrows(BadRequestException.class, () -> paymentService.createPaymentIntent("test@example.com", 1L));
    }
    @Test void confirmPayment_StripeNotConfigured_ShouldThrow() {
        when(stripeConfig.isStripeConfigured()).thenReturn(false);
        assertThrows(BadRequestException.class, () -> paymentService.confirmPayment("test@example.com", "pi_123"));
    }
}
