package com.example.Dr.VehicleCare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Dr.VehicleCare.security.JwtProvider;

@Service
public class JwtService {

    @Autowired
    private JwtProvider jwtProvider;

    // Generate JWT with userId + role
    public String generateToken(String userId, String role) {
        return jwtProvider.generateToken(userId, role);
    }

    // Validate token
    public boolean validateToken(String token) {
        return jwtProvider.validateToken(token);
    }

    public String extractUserId(String token) {
        return jwtProvider.extractUserId(token);
    }

    public String extractRole(String token) {
        return jwtProvider.extractRole(token);
    }
}
