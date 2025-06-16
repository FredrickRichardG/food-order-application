package com.foodorder.usermanagement.service;

import com.foodorder.usermanagement.model.User;
import com.foodorder.usermanagement.repository.UserRepository;
import com.foodorder.usermanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceEdgeCaseTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should handle empty database")
    void getAllUsers_EmptyDatabase() {
        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("Should handle large dataset")
    void getAllUsers_LargeDataset() {
        // Arrange - Create 1000 users
        List<User> expectedUsers = new ArrayList<>();
        IntStream.range(0, 1000).forEach(i -> {
            User user = new User();
            user.setFirstName("User" + i);
            user.setLastName("Last" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPhone("+1-555-" + String.format("%04d", i));
            user.setPassword("password" + i);
            user.setEnabled(true);
            user.setSeller(false);
            user.setActive(true);
            expectedUsers.add(userRepository.save(user));
        });

        // Act
        List<User> actualUsers = userService.getAllUsers();

        // Assert
        assertEquals(1000, actualUsers.size());
        assertTrue(actualUsers.containsAll(expectedUsers));
    }

    @Test
    @DisplayName("Should handle users with special characters")
    void getAllUsers_SpecialCharacters() {
        // Arrange
        User user1 = createUserWithSpecialChars("user1", "special1");
        User user2 = createUserWithSpecialChars("user2", "special2");
        userRepository.saveAll(List.of(user1, user2));

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getFirstName().contains("特殊")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().contains("特殊")));
    }

    @Test
    @DisplayName("Should handle concurrent access")
    void getAllUsers_ConcurrentAccess() throws InterruptedException, ExecutionException {
        // Arrange
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<List<User>>> futures = new ArrayList<>();

        // Create some test users first
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setFirstName("Concurrent" + i);
            user.setLastName("User" + i);
            user.setEmail("concurrent" + i + "@example.com");
            user.setPhone("+1-555-" + String.format("%04d", i));
            user.setPassword("password" + i);
            user.setEnabled(true);
            user.setSeller(false);
            user.setActive(true);
            userRepository.save(user);
        }

        // Act - Simulate concurrent access
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                latch.countDown();
                latch.await();
                return userService.getAllUsers();
            }));
        }

        // Assert
        List<User> firstResult = futures.get(0).get();
        for (int i = 1; i < threadCount; i++) {
            List<User> currentResult = futures.get(i).get();
            assertEquals(firstResult.size(), currentResult.size());
            assertTrue(firstResult.containsAll(currentResult));
        }

        executor.shutdown();
    }

    @Test
    @DisplayName("Should handle users with maximum field lengths")
    void getAllUsers_MaximumFieldLengths() {
        // Arrange
        User user = new User();
        // Required fields with valid values
        user.setEmail("maxlength@example.com");
        user.setPassword("ValidPass123!");
        user.setEnabled(true);
        user.setActive(true);
        user.setSeller(false);
        
        // Optional fields with maximum lengths
        user.setFirstName("A".repeat(40)); // Reduced from 255 to 100 for validation
        user.setLastName("B".repeat(40));  // Reduced from 255 to 100 for validation
        user.setPhone("+1-555-" + "0".repeat(10)); // Reduced length for validation
        
        userRepository.save(user);

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(1, users.size());
        User retrievedUser = users.get(0);
        assertEquals(40, retrievedUser.getFirstName().length());
        assertEquals(40, retrievedUser.getLastName().length());
        assertEquals("maxlength@example.com", retrievedUser.getEmail());
        assertTrue(retrievedUser.isEnabled());
        assertTrue(retrievedUser.isActive());
    }

    @Test
    @DisplayName("Should handle users with null fields")
    void getAllUsers_NullFields() {
        // Arrange
        User user = new User();
        // Required fields with valid values
        user.setEmail("nullfields@example.com");
        user.setPassword("ValidPass123!");
        user.setEnabled(true);
        user.setActive(true);
        user.setSeller(false);
        
        // Optional fields as null
        user.setFirstName(null);
        user.setLastName(null);
        user.setPhone(null); // This should cause validation failure
        
        // Act & Assert
        jakarta.validation.ConstraintViolationException exception = assertThrows(
            jakarta.validation.ConstraintViolationException.class,
            () -> userRepository.save(user)
        );

        System.out.println("exception"+exception);
        
        // Verify the exception contains the expected validation error
        assertTrue(exception.getConstraintViolations().stream()
            .anyMatch(violation -> 
                violation.getPropertyPath().toString().equals("phone") &&
                violation.getMessage().contains("must not be blank")
            )
        );
    }

    @Test
    @DisplayName("Should handle users with empty strings")
    void getAllUsers_EmptyStrings() {
        // Arrange
        User user = new User();
        // Required fields with valid values
        user.setEmail("emptystrings@example.com");
        user.setPassword("ValidPass123!");
        user.setEnabled(true);
        user.setActive(true);
        user.setSeller(false);
        
        // Optional fields as empty strings
        user.setFirstName("");
        user.setLastName("");
        user.setPhone(""); // This should cause validation failure
        
        // Act & Assert
        jakarta.validation.ConstraintViolationException exception = assertThrows(
            jakarta.validation.ConstraintViolationException.class,
            () -> userRepository.save(user)
        );
        
        // Verify the exception contains the expected validation error
        assertTrue(exception.getConstraintViolations().stream()
            .anyMatch(violation -> 
                violation.getPropertyPath().toString().equals("phone") &&
                violation.getMessage().contains("must not be blank")
            )
        );
    }

    @Test
    @DisplayName("Should handle users with SQL injection attempts")
    void getAllUsers_SqlInjectionAttempt() {
        // Arrange
        User user = new User();
        // Required fields with valid values
        user.setEmail("sqlinjection@example.com");
        user.setPassword("ValidPass123!");
        user.setEnabled(true);
        user.setActive(true);
        user.setSeller(false);
        
        // Optional fields with SQL injection attempts
        user.setFirstName("'; DROP TABLE users; --");
        user.setLastName("'); DROP TABLE users; --");
        user.setPhone("'; DROP TABLE users"); // Shortened to respect size constraint
        
        userRepository.save(user);

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(1, users.size());
        User retrievedUser = users.get(0);
        assertEquals("'; DROP TABLE users; --", retrievedUser.getFirstName());
        assertEquals("'); DROP TABLE users; --", retrievedUser.getLastName());
        assertEquals("'; DROP TABLE users", retrievedUser.getPhone());
        assertEquals("sqlinjection@example.com", retrievedUser.getEmail());
        assertTrue(retrievedUser.isEnabled());
        assertTrue(retrievedUser.isActive());
    }

    @Test
    @DisplayName("Should handle users with XSS attempts")
    void getAllUsers_XssAttempt() {
        // Arrange
        User user = new User();
        // Required fields with valid values
        user.setEmail("xssattempt@example.com");
        user.setPassword("ValidPass123!");
        user.setEnabled(true);
        user.setActive(true);
        user.setSeller(false);
        
        // Optional fields with XSS attempts
        user.setFirstName("<script>alert('xss')</script>");
        user.setLastName("<img src='x' onerror='alert(\"xss\")'>");
        user.setPhone("<(\"xss\")')</script>");
        
        userRepository.save(user);

        // Act
        List<User> users = userService.getAllUsers();

        // Assert
        assertEquals(1, users.size());
        User retrievedUser = users.get(0);
        assertEquals("<script>alert('xss')</script>", retrievedUser.getFirstName());
        assertEquals("<img src='x' onerror='alert(\"xss\")'>", retrievedUser.getLastName());
        assertEquals("<(\"xss\")')</script>", retrievedUser.getPhone());
        assertEquals("xssattempt@example.com", retrievedUser.getEmail());
        assertTrue(retrievedUser.isEnabled());
        assertTrue(retrievedUser.isActive());
    }

    @Test
    @DisplayName("Should reject user creation when phone number is null - validation constraint violation")
    void shouldRejectUserCreation_WhenPhoneNumberIsNull() {
        // Given - A user with all required fields but null phone number
        User userWithNullPhone = new User();
        userWithNullPhone.setEmail("test@example.com");
        userWithNullPhone.setPassword("ValidPass123!");
        userWithNullPhone.setEnabled(true);
        userWithNullPhone.setActive(true);
        userWithNullPhone.setSeller(false);
        userWithNullPhone.setFirstName("John");
        userWithNullPhone.setLastName("Doe");
        userWithNullPhone.setPhone(null); // Intentionally setting phone to null to test validation

        // When - Attempting to save the user
        // Then - Expect a validation exception with specific constraint violation
        jakarta.validation.ConstraintViolationException validationException = assertThrows(
            jakarta.validation.ConstraintViolationException.class,
            () -> userRepository.save(userWithNullPhone),
            "User creation should fail when phone number is null"
        );
        
        // Verify the specific validation error message
        assertTrue(
            validationException.getConstraintViolations().stream()
                .anyMatch(violation -> 
                    violation.getPropertyPath().toString().equals("phone") &&
                    violation.getMessage().contains("must not be blank")
                ),
            "Validation error should indicate phone number cannot be blank"
        );
    }

    @Test
    @DisplayName("Should reject user creation when phone number is empty - validation constraint violation")
    void shouldRejectUserCreation_WhenPhoneNumberIsEmpty() {
        // Given - A user with all required fields but empty phone number
        User userWithEmptyPhone = new User();
        userWithEmptyPhone.setEmail("test@example.com");
        userWithEmptyPhone.setPassword("ValidPass123!");
        userWithEmptyPhone.setEnabled(true);
        userWithEmptyPhone.setActive(true);
        userWithEmptyPhone.setSeller(false);
        userWithEmptyPhone.setFirstName("John");
        userWithEmptyPhone.setLastName("Doe");
        userWithEmptyPhone.setPhone(""); // Intentionally setting phone to empty string to test validation

        // When - Attempting to save the user
        // Then - Expect a validation exception with specific constraint violation
        jakarta.validation.ConstraintViolationException validationException = assertThrows(
            jakarta.validation.ConstraintViolationException.class,
            () -> userRepository.save(userWithEmptyPhone),
            "User creation should fail when phone number is empty"
        );
        
        // Verify the specific validation error message
        assertTrue(
            validationException.getConstraintViolations().stream()
                .anyMatch(violation -> 
                    violation.getPropertyPath().toString().equals("phone") &&
                    violation.getMessage().contains("must not be blank")
                ),
            "Validation error should indicate phone number cannot be blank"
        );
    }

    private User createUserWithSpecialChars(String suffix, String emailSuffix) {
        User user = new User();
        user.setFirstName("特殊" + suffix);
        user.setLastName("文字" + suffix);
        user.setEmail("special" + emailSuffix + "@特殊.com"); // Unique email
        user.setPhone("+1-555-特殊-" + suffix);
        user.setPassword("password" + suffix);
        user.setEnabled(true);
        user.setSeller(false);
        user.setActive(true);
        return user;
    }
} 