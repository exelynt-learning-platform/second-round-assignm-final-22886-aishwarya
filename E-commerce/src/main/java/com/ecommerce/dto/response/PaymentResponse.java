package com.ecommerce.dto.response;

public class PaymentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    private Long orderId;

    public PaymentResponse() {}
    public PaymentResponse(String clientSecret, String paymentIntentId, String status, Long orderId) {
        this.clientSecret = clientSecret; this.paymentIntentId = paymentIntentId;
        this.status = status; this.orderId = orderId;
    }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}
