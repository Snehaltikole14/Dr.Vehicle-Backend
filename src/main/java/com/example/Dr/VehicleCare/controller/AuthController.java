package com.example.Dr.VehicleCare.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Dr.VehicleCare.dto.SignUpRequest;
import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.UserRole;
import com.example.Dr.VehicleCare.service.JwtService;
import com.example.Dr.VehicleCare.service.OtpService;
import com.example.Dr.VehicleCare.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final OtpService otpService;

    // ===================== SIGNUP =====================
    @PostMapping("/signup/request-otp")
    public ResponseEntity<?> requestSignupOtp(@RequestBody Map<String, String> request) {

        String phone = request.get("phone");

        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }

        if (userService.findByPhone(phone).isPresent()) {
            return ResponseEntity.badRequest().body("Phone number already registered");
        }

        otpService.generateOtp(phone);
        return ResponseEntity.ok("OTP sent to your phone number");
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<?> verifySignupOtp(@Valid @RequestBody SignUpRequest request) {

        if (!otpService.verifyOtp(request.getPhone(), request.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }

        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setPasswordHash(request.getPassword()); // RAW password (encoded in service)

        // ❗ NEVER allow signup as ADMIN
        user.setRole(UserRole.CUSTOMER);

        return ResponseEntity.ok(userService.registerCustomer(user));
    }

    // ===================== LOGIN =====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        String loginId = request.get("emailOrName");
        String password = request.get("password");

        if (loginId == null || password == null) {
            return ResponseEntity.badRequest().body("Login ID and password required");
        }

        Optional<User> optionalUser = userService.findByLoginId(loginId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        User user = optionalUser.get();

        // ✅ CRITICAL NPE SAFETY
        if (user.getPasswordHash() == null) {
            return ResponseEntity.badRequest().body("Password not set for this user");
        }

        if (!userService.validatePassword(user, password)) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getId().toString(),
                user.getRole().name()
        );

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "role", user.getRole().name(),
                "token", token
        ));
    }

    // ===================== FORGOT PASSWORD =====================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {

        String phone = request.get("phone");

        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }

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

        if (phone == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body("All fields are required");
        }

        User user = userService.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!otpService.verifyOtp(phone, otp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }

        userService.changeUserPassword(user, newPassword);
        return ResponseEntity.ok("Password reset successfully");
    }
}
