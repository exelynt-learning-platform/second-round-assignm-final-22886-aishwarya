package com.ecommerce.controller;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(Authentication auth, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", orderService.createOrderFromCart(auth.getName(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orderService.getUserOrders(auth.getName())));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(Authentication auth, @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", orderService.getOrderById(auth.getName(), orderId)));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(Authentication auth, @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", orderService.cancelOrder(auth.getName(), orderId)));
    }
}
