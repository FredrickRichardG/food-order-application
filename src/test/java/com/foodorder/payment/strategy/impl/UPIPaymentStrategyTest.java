package com.foodorder.payment.strategy.impl;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class UPIPaymentStrategyTest {

    private UPIPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new UPIPaymentStrategy();
    }

    @Test
    void processPayment_WithValidDetails_ShouldSucceed() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("UPI")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .upiId("user@upi")
                .build();

        // When
        PaymentResult result = strategy.processPayment(details);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertEquals("UPI", result.getPaymentType());
        assertEquals("ORDER123", result.getOrderId());
    }

    @Test
    void processPayment_WithInvalidUPIId_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("UPI")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .upiId("invalid-upi") // Invalid UPI ID format
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void processPayment_WithNullUPIId_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("UPI")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .upiId(null)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void supports_WithUPIType_ShouldReturnTrue() {
        assertTrue(strategy.supports("UPI"));
    }

    @Test
    void supports_WithOtherType_ShouldReturnFalse() {
        assertFalse(strategy.supports("CREDIT_CARD"));
    }

    @Test
    void getPaymentType_ShouldReturnUPI() {
        assertEquals("UPI", strategy.getPaymentType());
    }
} 