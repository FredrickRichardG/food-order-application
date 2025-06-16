package com.foodorder.usermanagement.repository;

import com.foodorder.usermanagement.model.Role;
import com.foodorder.usermanagement.model.Role.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(RoleType name);
    
    boolean existsByName(RoleType name);
} 