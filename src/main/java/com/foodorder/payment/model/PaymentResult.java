package com.foodorder.payment.model;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String message;
    private LocalDateTime timestamp;
    private String paymentType;
    private String orderId;
} 