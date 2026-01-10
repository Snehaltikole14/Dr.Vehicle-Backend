package com.example.Dr.VehicleCare.model;

import java.time.Instant;

import com.example.Dr.VehicleCare.model.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;


    @Column(nullable = false)
    private String passwordHash;

    private String phone;
    
    @Column(unique = true, nullable = true)
    private String email;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // CUSTOMER, TECHNICIAN, ADMIN

    private Instant createdAt = Instant.now();

    @Column(length = 6)
    private String otp;

    private Instant otpExpiry;


    
}


