package com.ecommerce.controller;

import com.ecommerce.dto.request.PaymentRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping("/create-intent")
    public ResponseEntity<ApiResponse<PaymentResponse>> createIntent(Authentication auth, @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment intent created",
                paymentService.createPaymentIntent(auth.getName(), request.getOrderId())));
    }

    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirm(Authentication auth, @PathVariable String paymentIntentId) {
        return ResponseEntity.ok(ApiResponse.success("Payment processed",
                paymentService.confirmPayment(auth.getName(), paymentIntentId)));
    }
}
