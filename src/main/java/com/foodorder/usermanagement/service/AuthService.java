package com.foodorder.usermanagement.service;

import com.foodorder.usermanagement.dto.request.LoginRequest;
import com.foodorder.usermanagement.dto.request.RegisterRequest;
import com.foodorder.usermanagement.dto.response.JwtResponse;
 
public interface AuthService {
    JwtResponse login(LoginRequest loginRequest);
    JwtResponse register(RegisterRequest registerRequest);
    void logout(String token);
} 