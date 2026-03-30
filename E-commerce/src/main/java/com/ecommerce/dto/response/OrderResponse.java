package com.ecommerce.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long orderId;
    private List<OrderItemResponse> items;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;
    private String shippingCountry;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime createdAt;

    public OrderResponse() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String s) { this.shippingAddress = s; }
    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String s) { this.shippingCity = s; }
    public String getShippingState() { return shippingState; }
    public void setShippingState(String s) { this.shippingState = s; }
    public String getShippingZipCode() { return shippingZipCode; }
    public void setShippingZipCode(String s) { this.shippingZipCode = s; }
    public String getShippingCountry() { return shippingCountry; }
    public void setShippingCountry(String s) { this.shippingCountry = s; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal priceAtPurchase;
        private BigDecimal subtotal;

        public OrderItemResponse() {}
        public OrderItemResponse(Long productId, String productName, Integer quantity, BigDecimal priceAtPurchase) {
            this.productId = productId; this.productName = productName;
            this.quantity = quantity; this.priceAtPurchase = priceAtPurchase;
            this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPriceAtPurchase() { return priceAtPurchase; }
        public void setPriceAtPurchase(BigDecimal p) { this.priceAtPurchase = p; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}
