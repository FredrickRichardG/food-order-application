package com.foodorder.usermanagement.service;

import com.foodorder.usermanagement.dto.UserDTO;
import com.foodorder.usermanagement.exception.ResourceNotFoundException;
import com.foodorder.usermanagement.exception.UserAlreadyExistsException;
import com.foodorder.usermanagement.model.User;
import com.foodorder.usermanagement.repository.UserRepository;
import com.foodorder.usermanagement.security.JwtTokenProvider;
import com.foodorder.usermanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("1234567890");
        testUser.setPassword("password");
        testUser.setEnabled(true);
        testUser.setSeller(false);
        testUser.setActive(true);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setFirstName("Test");
        testUserDTO.setLastName("User");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPhone("1234567890");
        testUserDTO.setPassword("password");
        testUserDTO.setEnabled(true);
        testUserDTO.setSeller(false);
        testUserDTO.setActive(true);
    }

    @Test
    @DisplayName("Should successfully get user by ID")
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFirstName(), result.getFirstName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found by ID")
    void getUserById_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    @DisplayName("Should successfully get all users")
    void getAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getEmail(), result.get(0).getEmail());
    }

    @Test
    @DisplayName("Should successfully update user with all fields")
    void updateUser_Success_AllFields() {
        // Arrange
        User updateUser = new User();
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        updateUser.setEmail("updated@example.com");
        updateUser.setPhone("9876543210");
        updateUser.setPassword("newPassword");
        updateUser.setEnabled(true);
        updateUser.setSeller(true);
        updateUser.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updateUser);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // Act
        User result = userService.updateUser(1L, updateUser);

        // Assert
        assertNotNull(result);
        assertEquals(updateUser.getFirstName(), result.getFirstName());
        assertEquals(updateUser.getLastName(), result.getLastName());
        assertEquals(updateUser.getEmail(), result.getEmail());
        assertEquals(updateUser.getPhone(), result.getPhone());
        assertEquals(updateUser.isEnabled(), result.isEnabled());
        assertEquals(updateUser.isSeller(), result.isSeller());
        assertEquals(updateUser.isActive(), result.isActive());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully update user with partial fields")
    void updateUser_Success_PartialFields() {
        // Arrange
        User updateUser = new User();
        updateUser.setFirstName("Updated");
        updateUser.setLastName("User");
        // Email and phone not set to test partial update

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verify that only specified fields were updated
            assertEquals("Updated", savedUser.getFirstName());
            assertEquals("User", savedUser.getLastName());
            assertEquals(testUser.getEmail(), savedUser.getEmail()); // Should retain old email
            assertEquals(testUser.getPhone(), savedUser.getPhone()); // Should retain old phone
            assertEquals(testUser.isEnabled(), savedUser.isEnabled()); // Should retain old enabled status
            assertEquals(testUser.isSeller(), savedUser.isSeller()); // Should retain old seller status
            assertEquals(testUser.isActive(), savedUser.isActive()); // Should retain old active status
            return savedUser;
        });

        // Act
        User result = userService.updateUser(1L, updateUser);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals(testUser.getEmail(), result.getEmail()); // Should retain old email
        assertEquals(testUser.getPhone(), result.getPhone()); // Should retain old phone
        assertEquals(testUser.isEnabled(), result.isEnabled()); // Should retain old enabled status
        assertEquals(testUser.isSeller(), result.isSeller()); // Should retain old seller status
        assertEquals(testUser.isActive(), result.isActive()); // Should retain old active status
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent user")
    void updateUser_NotFound() {
        // Arrange
        User updateUser = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, updateUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle empty/null fields during update")
    void updateUser_Success_EmptyFields() {
        // Arrange
        User updateUser = new User();
        updateUser.setFirstName(""); // Empty first name
        updateUser.setLastName(null); // Null last name

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updateUser);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getFirstName());
        assertNull(result.getLastName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle seller profile update")
    void updateUser_Success_SellerProfile() {
        // Arrange
        User updateUser = new User();
        updateUser.setSeller(true);
        updateUser.setBusinessName("Test Business");
        updateUser.setBusinessAddress("123 Business St");
        updateUser.setUpiId("test@upi");
        updateUser.setBankAccountNumber("1234567890");
        updateUser.setBankIfscCode("TEST123456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updateUser);

        // Act
        User result = userService.updateUser(1L, updateUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSeller());
        assertEquals(updateUser.getBusinessName(), result.getBusinessName());
        assertEquals(updateUser.getBusinessAddress(), result.getBusinessAddress());
        assertEquals(updateUser.getUpiId(), result.getUpiId());
        assertEquals(updateUser.getBankAccountNumber(), result.getBankAccountNumber());
        assertEquals(updateUser.getBankIfscCode(), result.getBankIfscCode());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully delete user")
    void deleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
    void deleteUser_NotFound() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should successfully change password")
    void changePassword_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);

        // Act
        userService.changePassword(1L, "oldPassword", "newPassword");

        // Assert
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when old password is invalid")
    void changePassword_InvalidOldPassword() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> userService.changePassword(1L, "wrongPassword", "newPassword"));
        verify(userRepository, never()).save(any(User.class));
    }
} 