package com.foodorder.payment.service;

import com.foodorder.payment.model.PaymentDetails;
import com.foodorder.payment.model.PaymentResult;
import com.foodorder.payment.service.impl.PaymentServiceImpl;
import com.foodorder.payment.strategy.PaymentStrategy;
import com.foodorder.payment.strategy.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private PaymentService paymentService;
    
    @Mock
    private CreditCardPaymentStrategy creditCardStrategy;
    
    @Mock
    private PayPalPaymentStrategy paypalStrategy;
    
    @Mock
    private BankTransferPaymentStrategy bankTransferStrategy;
    
    @Mock
    private UPIPaymentStrategy upiStrategy;
    
    @BeforeEach
    void setUp() {
        List<PaymentStrategy> strategies = Arrays.asList(
            creditCardStrategy,
            paypalStrategy,
            bankTransferStrategy,
            upiStrategy
        );
        paymentService = new PaymentServiceImpl(strategies);
    }
    
    @Test
    void processPayment_WithCreditCard_ShouldUseCreditCardStrategy() {
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
        
        PaymentResult expectedResult = PaymentResult.builder()
                .success(true)
                .transactionId("TXN123")
                .paymentType("CREDIT_CARD")
                .orderId("ORDER123")
                .build();
        
        when(creditCardStrategy.supports("CREDIT_CARD")).thenReturn(true);
        when(creditCardStrategy.processPayment(any())).thenReturn(expectedResult);
        
        // When
        PaymentResult result = paymentService.processPayment(details);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(creditCardStrategy).processPayment(details);
    }
    
    @Test
    void processPayment_WithPayPal_ShouldUsePayPalStrategy() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("PAYPAL")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .paypalEmail("user@example.com")
                .build();
        
        PaymentResult expectedResult = PaymentResult.builder()
                .success(true)
                .transactionId("TXN123")
                .paymentType("PAYPAL")
                .orderId("ORDER123")
                .build();
        
        when(paypalStrategy.supports("PAYPAL")).thenReturn(true);
        when(paypalStrategy.processPayment(any())).thenReturn(expectedResult);
        
        // When
        PaymentResult result = paymentService.processPayment(details);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(paypalStrategy).processPayment(details);
    }
    
    @Test
    void processPayment_WithUnsupportedType_ShouldThrowException() {
        // Given
        PaymentDetails details = PaymentDetails.builder()
                .paymentType("UNSUPPORTED")
                .orderId("ORDER123")
                .amount(new BigDecimal("100.00"))
                .build();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> paymentService.processPayment(details));
    }
    
    @Test
    void supportsPaymentType_WithSupportedType_ShouldReturnTrue() {
        // Given
        when(creditCardStrategy.supports("CREDIT_CARD")).thenReturn(true);
        
        // When
        boolean supports = paymentService.supportsPaymentType("CREDIT_CARD");
        
        // Then
        assertTrue(supports);
    }
    
    @Test
    void supportsPaymentType_WithUnsupportedType_ShouldReturnFalse() {
        // Given
        when(creditCardStrategy.supports("UNSUPPORTED")).thenReturn(false);
        when(paypalStrategy.supports("UNSUPPORTED")).thenReturn(false);
        when(bankTransferStrategy.supports("UNSUPPORTED")).thenReturn(false);
        when(upiStrategy.supports("UNSUPPORTED")).thenReturn(false);
        
        // When
        boolean supports = paymentService.supportsPaymentType("UNSUPPORTED");
        
        // Then
        assertFalse(supports);
    }
} 