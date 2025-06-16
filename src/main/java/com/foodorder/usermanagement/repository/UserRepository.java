package com.foodorder.usermanagement.repository;

import com.foodorder.usermanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.isSeller = true")
    List<User> findAllSellers();
    
    @Query("SELECT u FROM User u WHERE u.isSeller = false")
    List<User> findAllCustomers();
} 