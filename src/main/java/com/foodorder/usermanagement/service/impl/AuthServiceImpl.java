package com.foodorder.usermanagement.service.impl;

import com.foodorder.usermanagement.dto.request.LoginRequest;
import com.foodorder.usermanagement.dto.request.RegisterRequest;
import com.foodorder.usermanagement.dto.response.JwtResponse;
import com.foodorder.usermanagement.exception.ResourceAlreadyExistsException;
import com.foodorder.usermanagement.model.Role;
import com.foodorder.usermanagement.model.User;
import com.foodorder.usermanagement.repository.RoleRepository;
import com.foodorder.usermanagement.repository.UserRepository;
import com.foodorder.usermanagement.security.JwtTokenProvider;
import com.foodorder.usermanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        User user = (User) authentication.getPrincipal();
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new JwtResponse(jwt, user.getId(), user.getEmail(), roles);
    }

    @Override
    @Transactional
    public JwtResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already taken");
        }


        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setSeller(registerRequest.isSeller());

        if (registerRequest.isSeller()) {
            user.setBusinessName(registerRequest.getBusinessName());
            user.setBusinessAddress(registerRequest.getBusinessAddress());
            user.setUpiId(registerRequest.getUpiId());
            user.setBankAccountNumber(registerRequest.getBankAccountNumber());
            user.setBankIfscCode(registerRequest.getBankIfscCode());
        }

        Role userRole = roleRepository.findByName(
            registerRequest.isSeller() ? Role.RoleType.ROLE_SELLER : Role.RoleType.ROLE_CUSTOMER
        ).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        
        user.addRole(userRole);
        userRepository.save(user);

        return login(new LoginRequest(registerRequest.getEmail(), registerRequest.getPassword()));
    }

    @Override
    public void logout(String token) {
        // In a stateless JWT implementation, we don't need to do anything on the server side
        // The client should remove the token
        SecurityContextHolder.clearContext();
    }
} 