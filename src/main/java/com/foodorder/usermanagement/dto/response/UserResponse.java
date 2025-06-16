package com.foodorder.usermanagement.dto.response;

import lombok.Data;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Set<String> roles;
    private boolean seller;
    private String businessName;
    private String businessAddress;
    private String upiId;
    private String bankAccountNumber;
    private String bankIfscCode;
} 