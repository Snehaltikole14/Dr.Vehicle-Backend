package com.example.Dr.VehicleCare.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Dr.VehicleCare.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    
    
    Optional<User> findByEmailOrName(String email, String name);
}