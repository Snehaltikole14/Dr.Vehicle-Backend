package com.example.Dr.VehicleCare.security;



import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.repository.UserRepository;

@Service
public class CustomUserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

