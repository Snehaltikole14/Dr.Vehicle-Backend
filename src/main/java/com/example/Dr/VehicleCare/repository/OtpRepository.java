package com.example.Dr.VehicleCare.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Dr.VehicleCare.model.Otp;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailAndCode(String email, String code);
    void deleteByEmail(String email);
}

