package com.foodorder.payment.strategy.impl;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PayPalPaymentStrategyTest {

    private PayPalPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PayPalPaymentStrategy();
    }

    @Test
    void processPayment_WithValidDetails_ShouldSucceed() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("PAYPAL")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .paypalEmail("user@example.com")
                .build();

        // When
        PaymentResult result = strategy.processPayment(details);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertEquals("PAYPAL", result.getPaymentType());
        assertEquals("ORDER123", result.getOrderId());
    }

    @Test
    void processPayment_WithInvalidEmail_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("PAYPAL")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .paypalEmail("invalid-email") // Invalid email format
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void processPayment_WithNullEmail_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("PAYPAL")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .paypalEmail(null)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void supports_WithPayPalType_ShouldReturnTrue() {
        assertTrue(strategy.supports("PAYPAL"));
    }

    @Test
    void supports_WithOtherType_ShouldReturnFalse() {
        assertFalse(strategy.supports("CREDIT_CARD"));
    }

    @Test
    void getPaymentType_ShouldReturnPayPal() {
        assertEquals("PAYPAL", strategy.getPaymentType());
    }
} 