package com.foodorder.usermanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private boolean enabled;
    private boolean isSeller;
    private boolean isActive;
    private String businessName;
    private String businessAddress;
    private String upiId;
    private String bankAccountNumber;
    private String bankIfscCode;
} 