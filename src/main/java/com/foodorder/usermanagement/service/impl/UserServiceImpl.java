package com.foodorder.usermanagement.service.impl;

import com.foodorder.usermanagement.dto.UserDTO;
import com.foodorder.usermanagement.exception.ResourceNotFoundException;
import com.foodorder.usermanagement.model.User;
import com.foodorder.usermanagement.repository.UserRepository;
import com.foodorder.usermanagement.security.JwtTokenProvider;
import com.foodorder.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getAllSellers() {
        return userRepository.findAllSellers();
    }

    @Override
    public List<User> getAllCustomers() {
        return userRepository.findAllCustomers();
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        // Authorization check: only the user or admin can update
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        if (!currentUser.getId().equals(id) && !currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"))) {
            throw new AccessDeniedException("Not authorized to update this user");
        }
        // Input validation
        if (userDetails.getEmail() != null && !EmailValidator.getInstance().isValid(userDetails.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (userDetails.getPassword() != null && userDetails.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        // TODO: Add rate limiting and logging for updateUser
        User user = getUserById(id);
        
        // Update fields only if they are not null
        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }

        user.setLastName(userDetails.getLastName());

        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        if (userDetails.isEnabled() != user.isEnabled()) {
            user.setEnabled(userDetails.isEnabled());
        }
        if (userDetails.isSeller() != user.isSeller()) {
            user.setSeller(userDetails.isSeller());
        }
        if (userDetails.isActive() != user.isActive()) {
            user.setActive(userDetails.isActive());
        }
        
        // Update seller-specific fields if user is a seller
        if (userDetails.isSeller()) {
            if (userDetails.getBusinessName() != null) {
                user.setBusinessName(userDetails.getBusinessName());
            }
            if (userDetails.getBusinessAddress() != null) {
                user.setBusinessAddress(userDetails.getBusinessAddress());
            }
            if (userDetails.getUpiId() != null) {
                user.setUpiId(userDetails.getUpiId());
            }
            if (userDetails.getBankAccountNumber() != null) {
                user.setBankAccountNumber(userDetails.getBankAccountNumber());
            }
            if (userDetails.getBankIfscCode() != null) {
                user.setBankIfscCode(userDetails.getBankIfscCode());
            }
        }
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Authorization check: only the user or admin can delete
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        if (!currentUser.getId().equals(id) && !currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"))) {
            throw new AccessDeniedException("Not authorized to delete this user");
        }
        // TODO: Add rate limiting and logging for deleteUser
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        // Authorization check: only the user or admin can change password
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        if (!currentUser.getId().equals(id) && !currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"))) {
            throw new AccessDeniedException("Not authorized to change password for this user");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }
        // TODO: Add rate limiting and logging for changePassword
        User user = getUserById(id);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
} 