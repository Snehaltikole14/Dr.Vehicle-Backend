package com.example.Dr.VehicleCare.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    Optional<User> findByPhone(String phone);
    
    
    Optional<User> findByEmailOrName(String email, String name);
    long countByRole(UserRole role);
    List<User> findByRole(UserRole role);
  Optional<User> findByPhoneOrNameOrEmail(
        String phone,
        String name,
        String email
    );

}



