package com.foodorder.payment.strategy.impl;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import com.foodorder.payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class UPIPaymentStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(PaymentDetails paymentDetails) {
        // Validate UPI details
        validateUPIDetails(paymentDetails);
        
        // Simulate UPI payment processing
        // In a real application, this would integrate with UPI payment gateway
        return PaymentResult.builder()
                .success(true)
                .transactionId(UUID.randomUUID().toString())
                .message("UPI payment processed successfully")
                .timestamp(LocalDateTime.now())
                .paymentType("UPI")
                .orderId(paymentDetails.getOrderId())
                .build();
    }
    
    @Override
    public boolean supports(String paymentType) {
        return "UPI".equalsIgnoreCase(paymentType);
    }
    
    @Override
    public String getPaymentType() {
        return "UPI";
    }
    
    private void validateUPIDetails(PaymentDetails details) {
        if (details.getUpiId() == null || !details.getUpiId().matches("^[a-zA-Z0-9._-]+@[a-zA-Z]{3,}$")) {
            throw new IllegalArgumentException("Invalid UPI ID format");
        }
    }
} 