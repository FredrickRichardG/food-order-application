package com.foodorder.payment.strategy.impl;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import com.foodorder.payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class BankTransferPaymentStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(PaymentDetails paymentDetails) {
        // Validate bank transfer details
        validateBankTransferDetails(paymentDetails);
        
        // Simulate bank transfer processing
        // In a real application, this would integrate with banking APIs
        return PaymentResult.builder()
                .success(true)
                .transactionId(UUID.randomUUID().toString())
                .message("Bank transfer initiated successfully")
                .timestamp(LocalDateTime.now())
                .paymentType("BANK_TRANSFER")
                .orderId(paymentDetails.getOrderId())
                .build();
    }
    
    @Override
    public boolean supports(String paymentType) {
        return "BANK_TRANSFER".equalsIgnoreCase(paymentType);
    }
    
    @Override
    public String getPaymentType() {
        return "BANK_TRANSFER";
    }
    
    private void validateBankTransferDetails(PaymentDetails details) {
        if (details.getAccountNumber() == null || details.getAccountNumber().length() < 9) {
            throw new IllegalArgumentException("Invalid account number");
        }
        if (details.getBankName() == null || details.getBankName().trim().isEmpty()) {
            throw new IllegalArgumentException("Bank name is required");
        }
        if (details.getIfscCode() == null || !details.getIfscCode().matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
            throw new IllegalArgumentException("Invalid IFSC code");
        }
    }
} 