package com.foodorder.payment.service.impl;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import com.foodorder.payment.service.PaymentService;
import com.foodorder.payment.strategy.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    
    private final List<PaymentStrategy> paymentStrategies;
    
    @Autowired
    public PaymentServiceImpl(List<PaymentStrategy> paymentStrategies) {
        this.paymentStrategies = paymentStrategies;
    }
    
    @Override
    public PaymentResult processPayment(PaymentDetails paymentDetails) {
        PaymentStrategy strategy = findStrategy(paymentDetails.getPaymentType());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + paymentDetails.getPaymentType());
        }
        return strategy.processPayment(paymentDetails);
    }
    
    @Override
    public boolean supportsPaymentType(String paymentType) {
        return findStrategy(paymentType) != null;
    }
    
    private PaymentStrategy findStrategy(String paymentType) {
        return paymentStrategies.stream()
                .filter(strategy -> strategy.supports(paymentType))
                .findFirst()
                .orElse(null);
    }
} 