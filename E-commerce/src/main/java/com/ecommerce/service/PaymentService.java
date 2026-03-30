package com.ecommerce.service;

import com.ecommerce.config.StripeConfig;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final StripeConfig stripeConfig;

    public PaymentService(OrderService orderService, OrderRepository orderRepository, StripeConfig stripeConfig) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.stripeConfig = stripeConfig;
    }

    @Transactional
    public PaymentResponse createPaymentIntent(String email, Long orderId) {
        if (!stripeConfig.isStripeConfigured())
            throw new BadRequestException("Stripe is not configured. Set stripe.secret-key and stripe.enabled=true in application.properties");

        Order order = orderService.getOrderEntityById(email, orderId);
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED)
            throw new BadRequestException("Payment already completed for this order");
        if (order.getOrderStatus() == OrderStatus.CANCELLED)
            throw new BadRequestException("Cannot pay for a cancelled order");

        try {
            long cents = order.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue();
            PaymentIntent pi = PaymentIntent.create(PaymentIntentCreateParams.builder()
                    .setAmount(cents).setCurrency("usd")
                    .putMetadata("orderId", order.getId().toString())
                    .putMetadata("userEmail", email).build());
            order.setStripePaymentIntentId(pi.getId());
            orderRepository.save(order);
            return new PaymentResponse(pi.getClientSecret(), pi.getId(), pi.getStatus(), order.getId());
        } catch (StripeException e) {
            logger.error("Stripe payment intent creation failed for order {}", orderId);
            throw new BadRequestException("Payment processing failed. Please try again.");
        }
    }

    @Transactional
    public PaymentResponse confirmPayment(String email, String paymentIntentId) {
        if (!stripeConfig.isStripeConfigured())
            throw new BadRequestException("Stripe is not configured. Set stripe.secret-key and stripe.enabled=true in application.properties");
        try {
            PaymentIntent pi = PaymentIntent.retrieve(paymentIntentId);
            String orderId = pi.getMetadata().get("orderId");
            if (orderId == null) throw new BadRequestException("Invalid payment intent");
            Order order = orderService.getOrderEntityById(email, Long.parseLong(orderId));

            if ("succeeded".equals(pi.getStatus())) {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
                order.setOrderStatus(OrderStatus.CONFIRMED);
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
            }
            orderRepository.save(order);
            return new PaymentResponse(null, paymentIntentId, pi.getStatus(), order.getId());
        } catch (StripeException e) {
            logger.error("Stripe payment confirmation failed for intent {}", paymentIntentId);
            throw new BadRequestException("Payment confirmation failed. Please try again.");
        }
    }
}
