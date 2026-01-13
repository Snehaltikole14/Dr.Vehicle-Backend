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

        boolean verified = otpService.verifyOtp(request.getPhone(), request.getOtp());

        if (!verified) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }

        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setPasswordHash(request.getPassword());

        // ✅ Always assign role (avoid NULL)
        UserRole role = request.getRole() != null
                ? UserRole.valueOf(request.getRole())
                : UserRole.CUSTOMER;

        user.setRole(role);

        return ResponseEntity.ok(userService.registerCustomer(user));
    }

    // ===================== LOGIN =====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {

        String loginId = request.get("emailOrName"); // email / phone / name
        String password = request.get("password");

        if (loginId == null || password == null) {
            return ResponseEntity.badRequest().body("Login ID and password required");
        }

        Optional<User> optionalUser = userService.findByLoginId(loginId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        User user = optionalUser.get();

        if (!userService.validatePassword(user, password)) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        // ✅ SAFETY: role must NEVER be null
        UserRole role = user.getRole() != null
                ? user.getRole()
                : UserRole.CUSTOMER;

        String token = jwtService.generateToken(
                String.valueOf(user.getId()),
                role.name()
        );

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "role", role.name(),
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
