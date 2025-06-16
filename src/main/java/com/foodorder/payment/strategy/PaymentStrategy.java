package com.foodorder.payment.strategy;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;

public interface PaymentStrategy {
    PaymentResult processPayment(PaymentDetails paymentDetails);
    boolean supports(String paymentType);
    String getPaymentType();
} 