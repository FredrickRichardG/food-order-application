package com.foodorder.usermanagement.service;

import com.foodorder.usermanagement.dto.UserDTO;
import com.foodorder.usermanagement.model.User;
import java.util.List;

public interface UserService {
    User getUserById(Long id);
    User getUserByEmail(String email);
    List<User> getAllUsers();
    List<User> getAllSellers();
    List<User> getAllCustomers();
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
    void changePassword(Long id, String oldPassword, String newPassword);
} 