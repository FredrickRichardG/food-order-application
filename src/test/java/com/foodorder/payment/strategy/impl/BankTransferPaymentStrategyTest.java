package com.foodorder.payment.strategy.impl;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class BankTransferPaymentStrategyTest {

    private BankTransferPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BankTransferPaymentStrategy();
    }

    @Test
    void processPayment_WithValidDetails_ShouldSucceed() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("BANK_TRANSFER")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .accountNumber("1234567890")
                .bankName("Test Bank")
                .ifscCode("TEST0123456")
                .build();

        // When
        PaymentResult result = strategy.processPayment(details);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertEquals("BANK_TRANSFER", result.getPaymentType());
        assertEquals("ORDER123", result.getOrderId());
    }

    @Test
    void processPayment_WithInvalidAccountNumber_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("BANK_TRANSFER")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .accountNumber("123") // Invalid account number
                .bankName("Test Bank")
                .ifscCode("TEST0123456")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void processPayment_WithInvalidIFSC_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("BANK_TRANSFER")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .accountNumber("1234567890")
                .bankName("Test Bank")
                .ifscCode("INVALID") // Invalid IFSC code
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void processPayment_WithNullBankName_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("BANK_TRANSFER")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .accountNumber("1234567890")
                .bankName(null)
                .ifscCode("TEST0123456")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> strategy.processPayment(details));
    }

    @Test
    void supports_WithBankTransferType_ShouldReturnTrue() {
        assertTrue(strategy.supports("BANK_TRANSFER"));
    }

    @Test
    void supports_WithOtherType_ShouldReturnFalse() {
        assertFalse(strategy.supports("CREDIT_CARD"));
    }

    @Test
    void getPaymentType_ShouldReturnBankTransfer() {
        assertEquals("BANK_TRANSFER", strategy.getPaymentType());
    }
} 