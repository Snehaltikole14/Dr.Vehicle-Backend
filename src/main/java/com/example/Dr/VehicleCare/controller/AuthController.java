package com.example.Dr.VehicleCare.controller;

import java.util.HashMap;
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

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private OtpService otpService;

    // ===================== SIGNUP (OTP REQUEST) =====================
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

    // ===================== SIGNUP (VERIFY OTP) =====================
    @PostMapping("/signup/verify-otp")
    public ResponseEntity<?> verifySignupOtp(@Valid @RequestBody SignUpRequest request) {

        if (!otpService.verifyOtp(request.getPhone(), request.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }

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

        String identifier = request.get("identifier");
        String password = request.get("password");

        if (identifier == null || password == null) {
            return ResponseEntity.badRequest().body("Identifier and password are required");
        }

        Optional<User> userOptional = userService.findByLoginId(identifier);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        User user = userOptional.get();

        if (!userService.validatePassword(user, password)) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = jwtService.generateToken(
                String.valueOf(user.getId()),
                user.getRole().name()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("role", user.getRole().name());

        return ResponseEntity.ok(response);
    }

    // ===================== FORGOT PASSWORD =====================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {

        String phone = request.get("phone");

        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }

        userService.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Phone number not registered"));

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
