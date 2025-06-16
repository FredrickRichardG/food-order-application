package com.foodorder.payment.service;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;

public interface PaymentService {
    PaymentResult processPayment(PaymentDetails paymentDetails);
    boolean supportsPaymentType(String paymentType);
} 