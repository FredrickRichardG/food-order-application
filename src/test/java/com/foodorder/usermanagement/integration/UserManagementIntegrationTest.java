package com.foodorder.usermanagement.integration;

import com.foodorder.usermanagement.dto.UserDTO;
import com.foodorder.usermanagement.model.User;
import com.foodorder.usermanagement.repository.UserRepository;
import com.foodorder.usermanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
class UserManagementIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

//    @LocalServerPort
    private int port=8080;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;
    private User testUser;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/users";
        
        // Clear database before each test
        userRepository.deleteAll();
        
        // Wait for database to be ready
        postgres.waitingFor(new org.testcontainers.containers.wait.strategy.WaitAllStrategy()
                .withStrategy(org.testcontainers.containers.wait.strategy.Wait.forListeningPort())
                .withStrategy(org.testcontainers.containers.wait.strategy.Wait.forLogMessage(".*database system is ready to accept connections.*", 1)));

        // Create test user with realistic data
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhone("+1-555-123-4567");
        testUser.setPassword("SecurePass123!");
        testUser.setEnabled(true);
        testUser.setSeller(false);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Setup headers with authentication
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAuthToken());
    }

    private String getAuthToken() {
        // For testing purposes, return a dummy token
        return "test-token";
    }

    @Test
    @DisplayName("Should create a new user with valid data")
    void createUser_Success() {
        // Arrange
        UserDTO newUser = new UserDTO();
        newUser.setFirstName("Jane");
        newUser.setLastName("Smith");
        newUser.setEmail("jane.smith@example.com");
        newUser.setPhone("+1-555-987-6543");
        newUser.setPassword("SecurePass456!");
        newUser.setEnabled(true);
        newUser.setSeller(false);
        newUser.setActive(true);

        HttpEntity<UserDTO> request = new HttpEntity<>(newUser, headers);

        // Act
        ResponseEntity<UserDTO> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                UserDTO.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newUser.getEmail(), response.getBody().getEmail());
        assertEquals(newUser.getFirstName(), response.getBody().getFirstName());
        
        // Verify database state
        Optional<User> savedUser = userRepository.findByEmail(newUser.getEmail());
        assertTrue(savedUser.isPresent());
        assertEquals(newUser.getFirstName(), savedUser.get().getFirstName());
    }

    @Test
    @DisplayName("Should handle database connection failure")
    void createUser_DatabaseFailure() {
        // Arrange
        postgres.stop(); // Simulate database failure
        
        UserDTO newUser = new UserDTO();
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setEmail("test@example.com");
        
        HttpEntity<UserDTO> request = new HttpEntity<>(newUser, headers);

        // Act & Assert
        assertThrows(ResourceAccessException.class, () -> {
            restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                UserDTO.class
            );
        });

        // Cleanup
        postgres.start();
    }

    @Test
    @DisplayName("Should handle concurrent user creation")
    void createUser_Concurrent() throws InterruptedException {
        // Arrange
        UserDTO user1 = createTestUserDTO("user1@example.com");
        UserDTO user2 = createTestUserDTO("user2@example.com");

        // Act
        Thread thread1 = new Thread(() -> {
            HttpEntity<UserDTO> request = new HttpEntity<>(user1, headers);
            restTemplate.exchange(baseUrl, HttpMethod.POST, request, UserDTO.class);
        });

        Thread thread2 = new Thread(() -> {
            HttpEntity<UserDTO> request = new HttpEntity<>(user2, headers);
            restTemplate.exchange(baseUrl, HttpMethod.POST, request, UserDTO.class);
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Assert
        List<User> users = userRepository.findAll();
        assertEquals(3, users.size()); // Original test user + 2 new users
    }

    @Test
    @DisplayName("Should handle invalid authentication")
    void createUser_InvalidAuth() {
        // Arrange
        HttpHeaders invalidHeaders = new HttpHeaders();
        invalidHeaders.setContentType(MediaType.APPLICATION_JSON);
        invalidHeaders.setBearerAuth("invalid-token");

        UserDTO newUser = createTestUserDTO("test@example.com");
        HttpEntity<UserDTO> request = new HttpEntity<>(newUser, invalidHeaders);

        // Act
        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                Object.class
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle rate limiting")
    void createUser_RateLimit() {
        // Arrange
        UserDTO newUser = createTestUserDTO("test@example.com");
        HttpEntity<UserDTO> request = new HttpEntity<>(newUser, headers);

        // Act - Make multiple requests in quick succession
        for (int i = 0; i < 10; i++) {
            restTemplate.exchange(baseUrl, HttpMethod.POST, request, UserDTO.class);
        }

        // Make one more request that should be rate limited
        ResponseEntity<Object> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                Object.class
        );

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
    }

    private UserDTO createTestUserDTO(String email) {
        UserDTO user = new UserDTO();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPhone("+1-555-000-0000");
        user.setPassword("SecurePass123!");
        user.setEnabled(true);
        user.setSeller(false);
        user.setActive(true);
        return user;
    }
} 