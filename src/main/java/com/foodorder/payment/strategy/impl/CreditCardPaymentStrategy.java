package com.foodorder.payment.strategy.impl;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import com.foodorder.payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(PaymentDetails paymentDetails) {
        // Validate credit card details
        validateCreditCardDetails(paymentDetails);
        
        // Simulate credit card payment processing
        // In a real application, this would integrate with a payment gateway
        return PaymentResult.builder()
                .success(true)
                .transactionId(UUID.randomUUID().toString())
                .message("Credit card payment processed successfully")
                .timestamp(LocalDateTime.now())
                .paymentType("CREDIT_CARD")
                .orderId(paymentDetails.getOrderId())
                .build();
    }
    
    @Override
    public boolean supports(String paymentType) {
        return "CREDIT_CARD".equalsIgnoreCase(paymentType);
    }
    
    @Override
    public String getPaymentType() {
        return "CREDIT_CARD";
    }
    
    private void validateCreditCardDetails(PaymentDetails details) {
        if (details.getCardNumber() == null || details.getCardNumber().length() != 16) {
            throw new IllegalArgumentException("Invalid card number");
        }
        if (details.getCardHolderName() == null || details.getCardHolderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Card holder name is required");
        }
        if (details.getExpiryDate() == null || !details.getExpiryDate().matches("\\d{2}/\\d{2}")) {
            throw new IllegalArgumentException("Invalid expiry date format (MM/YY)");
        }
        if (details.getCvv() == null || details.getCvv().length() != 3) {
            throw new IllegalArgumentException("Invalid CVV");
        }
    }
} 