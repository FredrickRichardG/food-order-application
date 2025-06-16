package com.foodorder.payment.strategy.impl;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import com.foodorder.payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PayPalPaymentStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(PaymentDetails paymentDetails) {
        // Validate PayPal details
        validatePayPalDetails(paymentDetails);
        
        // Simulate PayPal payment processing
        // In a real application, this would integrate with PayPal API
        return PaymentResult.builder()
                .success(true)
                .transactionId(UUID.randomUUID().toString())
                .message("PayPal payment processed successfully")
                .timestamp(LocalDateTime.now())
                .paymentType("PAYPAL")
                .orderId(paymentDetails.getOrderId())
                .build();
    }
    
    @Override
    public boolean supports(String paymentType) {
        return "PAYPAL".equalsIgnoreCase(paymentType);
    }
    
    @Override
    public String getPaymentType() {
        return "PAYPAL";
    }
    
    private void validatePayPalDetails(PaymentDetails details) {
        if (details.getPaypalEmail() == null || !details.getPaypalEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid PayPal email address");
        }
    }
} 