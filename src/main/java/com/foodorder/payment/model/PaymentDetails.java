package com.foodorder.payment.model;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentDetails {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentType;
    
    // Credit Card specific fields
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    
    // PayPal specific fields
    private String paypalEmail;
    
    // Bank Transfer specific fields
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    
    // UPI specific fields
    private String upiId;
} 