package com.foodorder.payment.strategy.impl;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CreditCardPaymentStrategyTest {

    private CreditCardPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CreditCardPaymentStrategy();
    }

    @Test
    void processPayment_WithValidDetails_ShouldSucceed() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("CREDIT_CARD")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvv("123")
                .build();

        // When
        PaymentResult result = strategy.processPayment(details);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertEquals("CREDIT_CARD", result.getPaymentType());
        assertEquals("ORDER123", result.getOrderId());
    }

    @Test
    void processPayment_WithInvalidCardNumber_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("CREDIT_CARD")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .cardNumber("123") // Invalid card number
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvv("123")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void processPayment_WithInvalidExpiryDate_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("CREDIT_CARD")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12-25") // Invalid format
                .cvv("123")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void processPayment_WithInvalidCVV_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("CREDIT_CARD")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvv("12") // Invalid CVV
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void supports_WithCreditCardType_ShouldReturnTrue() {
        assertTrue(strategy.supports("CREDIT_CARD"));
    }

    @Test
    void supports_WithOtherType_ShouldReturnFalse() {
        assertFalse(strategy.supports("PAYPAL"));
    }

    @Test
    void getPaymentType_ShouldReturnCreditCard() {
        assertEquals("CREDIT_CARD", strategy.getPaymentType());
    }
} 