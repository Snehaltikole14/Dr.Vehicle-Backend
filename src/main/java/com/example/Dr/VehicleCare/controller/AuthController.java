package com.example.Dr.VehicleCare.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Dr.VehicleCare.dto.SignUpRequest;
import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.UserRole;
import com.example.Dr.VehicleCare.service.EmailService;
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
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private OtpService otpService;
    @Autowired private EmailService emailService;

    // ===================== SIGNUP FLOW =====================
    @PostMapping("/signup/request-otp")
    public ResponseEntity<?> requestSignupOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String phone = request.get("phone");

        if (email == null || phone == null)
            return ResponseEntity.badRequest().body("Email and phone number are required");

        if (userService.findByEmail(email).isPresent())
            return ResponseEntity.badRequest().body("Email already exists");

        String otp = otpService.generateOtp(email, phone);

        try {
            emailService.sendSimpleMessage(email, "Dr.VehicleCare - Signup OTP",
                    "<p>Your OTP for signup is <b>" + otp + "</b>. It will expire in 5 minutes.</p>");
        } catch (Exception e) {
            System.out.println("Email sending failed: " + e.getMessage());
        }

        return ResponseEntity.ok("OTP sent to your phone and email");
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<?> verifySignupOtp(@Valid @RequestBody SignUpRequest request) {
        boolean verified = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!verified) return ResponseEntity.badRequest().body("Invalid or expired OTP");

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(request.getPassword()); // <-- raw password, encode in service
        user.setRole(request.getRole() != null ? UserRole.valueOf(request.getRole()) : UserRole.CUSTOMER);

        User registeredUser = userService.registerCustomer(user);
        return ResponseEntity.ok(registeredUser);
    }

    // ===================== LOGIN =====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String emailOrName = request.get("emailOrName");
        String password = request.get("password");

        if (emailOrName == null || password == null)
            return ResponseEntity.badRequest().body("Email/Name and password are required");

        Optional<User> optionalUser = userService.findByEmailOrName(emailOrName);
        if (optionalUser.isEmpty())
            return ResponseEntity.badRequest().body("Invalid credentials");

        User user = optionalUser.get();

        if (!userService.validatePassword(user, password))
            return ResponseEntity.badRequest().body("Invalid credentials");

        // Generate JWT
        String token = jwtService.generateToken(
            String.valueOf(user.getId()),  
            user.getRole().name()
        );

        return ResponseEntity.ok(Map.of(
            "id", user.getId(),              // <-- ADD THIS
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole().toString(),
            "token", token
        ));
    }



    // ===================== FORGOT PASSWORD =====================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not registered"));

        String otp = otpService.generateOtp(email, user.getPhone());

        try {
            emailService.sendSimpleMessage(email, "Dr.VehicleCare - Password Reset OTP",
                    "<p>Your OTP to reset password is <b>" + otp + "</b>. It expires in 5 minutes.</p>");
        } catch (Exception e) {
            System.out.println("Email sending failed: " + e.getMessage());
        }

        return ResponseEntity.ok("OTP sent to your registered phone and email for password reset");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        boolean verified = otpService.verifyOtp(email, otp);
        if (!verified) return ResponseEntity.badRequest().body("Invalid or expired OTP");

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.changeUserPassword(user, newPassword);
        return ResponseEntity.ok("Password reset successfully");
    }

    // ===================== TOKEN REFRESH =====================
//    @PostMapping("/refresh")
//    public ResponseEntity<?> refresh(@RequestParam String refreshToken) {
//        if (!jwtService.validateToken(refreshToken)) 
//            return ResponseEntity.badRequest().body("Invalid refresh token");
//
//        String email = jwtService.extractUsername(refreshToken);
//        String token = jwtService.generateToken(email);
//        return ResponseEntity.ok(Map.of("token", token));
//    }
}
