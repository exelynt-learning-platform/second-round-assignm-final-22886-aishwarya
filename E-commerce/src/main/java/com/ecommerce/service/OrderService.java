package com.ecommerce.service;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.OrderResponse.OrderItemResponse;
import com.ecommerce.entity.*;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        ProductRepository productRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Transactional
    public OrderResponse createOrderFromCart(String email, OrderRequest request) {
        User user = findUser(email);
        Cart cart = cartService.getCartForUser(email);
        if (cart.getItems().isEmpty())
            throw new BadRequestException("Cart is empty. Add items before placing an order.");

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZipCode(request.getShippingZipCode());
        order.setShippingCountry(request.getShippingCountry());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity())
                throw new BadRequestException("Insufficient stock for: " + product.getName());
            order.getItems().add(new OrderItem(order, product, cartItem.getQuantity(), product.getPrice()));
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        order.setTotalPrice(total);
        order = orderRepository.save(order);
        cartService.clearCart(email);
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String email) {
        User user = findUser(email);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String email, Long orderId) {
        return mapToResponse(getOrderEntityById(email, orderId));
    }

    @Transactional
    public Order getOrderEntityById(String email, Long orderId) {
        User user = findUser(email);
        return orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {
        Order order = getOrderEntityById(email, orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING)
            throw new BadRequestException("Only pending orders can be cancelled");
        order.setOrderStatus(OrderStatus.CANCELLED);
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
            productRepository.save(p);
        }
        return mapToResponse(orderRepository.save(order));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse r = new OrderResponse();
        r.setOrderId(order.getId());
        r.setTotalPrice(order.getTotalPrice());
        r.setShippingAddress(order.getShippingAddress());
        r.setShippingCity(order.getShippingCity());
        r.setShippingState(order.getShippingState());
        r.setShippingZipCode(order.getShippingZipCode());
        r.setShippingCountry(order.getShippingCountry());
        r.setOrderStatus(order.getOrderStatus().name());
        r.setPaymentStatus(order.getPaymentStatus().name());
        r.setCreatedAt(order.getCreatedAt());
        r.setItems(order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProduct().getId(), i.getProduct().getName(),
                        i.getQuantity(), i.getPriceAtPurchase()))
                .collect(Collectors.toList()));
        return r;
    }
}
