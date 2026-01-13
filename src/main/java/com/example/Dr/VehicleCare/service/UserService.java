package com.example.Dr.VehicleCare.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.Dr.VehicleCare.model.PasswordResetToken;
import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.UserRole;
import com.example.Dr.VehicleCare.repository.PasswordResetTokenRepository;
import com.example.Dr.VehicleCare.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    // ==================== FIND USERS ====================
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ==================== LOGIN SUPPORT ====================
    public Optional<User> findByLoginId(String loginId) {
        Optional<User> byEmail = userRepository.findByEmail(loginId);
        if (byEmail.isPresent()) return byEmail;

        Optional<User> byPhone = userRepository.findByPhone(loginId);
        if (byPhone.isPresent()) return byPhone;

        return userRepository.findByName(loginId);
    }

    // ==================== REGISTER ====================
    public User registerCustomer(User user) {
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        if (user.getRole() == null) user.setRole(UserRole.CUSTOMER);
        return userRepository.save(user);
    }

    // ==================== PASSWORD VALIDATION ====================
    public boolean validatePassword(User user, String rawPassword) {
        return user.getPasswordHash() != null &&
               passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    // ==================== CHANGE PASSWORD (LOGGED IN) ====================
    public void changePassword(Long userId, String oldPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ==================== CHANGE PASSWORD (FORGOT PASSWORD / OTP) ====================
    public void changeUserPassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
    }

    // ==================== CRUD ====================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);
        existing.setName(updatedUser.getName());
        existing.setPhone(updatedUser.getPhone());
        existing.setRole(updatedUser.getRole());
        if (updatedUser.getEmail() != null) {
            existing.setEmail(updatedUser.getEmail());
        }
        return userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw new RuntimeException("User not found");
        userRepository.deleteById(id);
    }
}
