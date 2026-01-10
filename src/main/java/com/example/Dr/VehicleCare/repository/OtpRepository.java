package com.example.Dr.VehicleCare.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Dr.VehicleCare.model.Otp;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    // ✅ Phone-based OTP
    Optional<Otp> findByPhoneAndCode(String phone, String code);

    void deleteByPhone(String phone);

    // Optional: keep email-based OTP for users who have email
    Optional<Otp> findByEmailAndCode(String email, String code);

    void deleteByEmail(String email);
}
