package com.example.Dr.VehicleCare.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Dr.VehicleCare.dto.SignUpRequest;
import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.UserRole;
import com.example.Dr.VehicleCare.service.JwtService;
import com.example.Dr.VehicleCare.service.OtpService;
import com.example.Dr.VehicleCare.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtService jwtService;
    @Autowired private OtpService otpService;

    // ===================== SIGNUP =====================

    @PostMapping("/signup/request-otp")
    public ResponseEntity<?> requestSignupOtp(@RequestBody Map<String, String> request) {

        String phone = request.get("phone");

        if (phone == null)
            return ResponseEntity.badRequest().body("Phone number is required");

        if (userService.findByPhone(phone).isPresent())
            return ResponseEntity.badRequest().body("Phone number already registered");

        otpService.generateOtp(phone);
        return ResponseEntity.ok("OTP sent to your phone number");
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<?> verifySignupOtp(@Valid @RequestBody SignUpRequest request) {

        boolean verified = otpService.verifyOtp(request.getPhone(), request.getOtp());
        if (!verified)
            return ResponseEntity.badRequest().body("Invalid or expired OTP");

        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setPasswordHash(request.getPassword());
        user.setRole(
                request.getRole() != null
                        ? UserRole.valueOf(request.getRole())
                        : UserRole.CUSTOMER
        );

        return ResponseEntity.ok(userService.registerCustomer(user));
    }

    // ===================== LOGIN =====================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        // 🔥 MATCH FRONTEND KEY
        String phoneOrName = request.get("emailOrName");
        String password = request.get("password");

        if (phoneOrName == null || password == null)
            return ResponseEntity.badRequest().body("Phone/Name and password are required");

        Optional<User> optionalUser = userService.findByPhoneOrName(phoneOrName);

        if (optionalUser.isEmpty())
            return ResponseEntity.badRequest().body("Invalid credentials");

        User user = optionalUser.get();

        if (!userService.validatePassword(user, password))
            return ResponseEntity.badRequest().body("Invalid credentials");

        String token = jwtService.generateToken(
                String.valueOf(user.getId()),
                user.getRole().name()
        );

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "phone", user.getPhone(),
                "role", user.getRole().name(),
                "token", token
        ));
    }

    // ===================== FORGOT PASSWORD (PHONE BASED) =====================

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {

        String phone = request.get("phone");

        if (phone == null)
            return ResponseEntity.badRequest().body("Phone number is required");

        User user = userService.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Phone not registered"));

        otpService.generateOtp(phone);

        return ResponseEntity.ok("OTP sent to your phone number");
    }

    // ===================== RESET PASSWORD =====================

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {

        String phone = request.get("phone");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        User user = userService.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean verified = otpService.verifyOtp(phone, otp);
        if (!verified)
            return ResponseEntity.badRequest().body("Invalid or expired OTP");

        userService.changeUserPassword(user, newPassword);
        return ResponseEntity.ok("Password reset successfully");
    }
}
