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
    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public Optional<User> findByPhone(String phone) {
    return userRepository.findByPhone(phone);
}


    public Optional<User> findByEmailOrName(String emailOrName) {
        return userRepository.findByEmailOrName(emailOrName, emailOrName);
    }

    // ==================== REGISTER CUSTOMER ====================
    public User registerCustomer(User user) {
        // Encode the raw password exactly once
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        if (user.getRole() == null) user.setRole(UserRole.CUSTOMER);
        return userRepository.save(user);
    }

    // ==================== PASSWORD VALIDATION ====================
    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    // ==================== CREATE PASSWORD RESET TOKEN ====================
    public void createPasswordResetTokenForUser(User user, String appUrl) {
        String token = UUID.randomUUID().toString();

        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(2))
                .build();

        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        tokenRepository.save(prt);

        String resetUrl = appUrl + "/api/users/resetPassword?token=" + token;
        String body = "To reset your password click the link below:\n"
                     + resetUrl
                     + "\nThis link expires in 2 hours.";

        emailService.sendSimpleMessage(user.getEmail(), "Password Reset Request", body);
    }

    // ==================== VALIDATE PASSWORD RESET TOKEN ====================
    public Optional<User> validatePasswordResetToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(PasswordResetToken::getUser);
    }

    // ==================== CHANGE USER PASSWORD ====================
    public void changeUserPassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
    }

    // ==================== UPDATE USER ====================
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Update user
    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);

        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPhone(updatedUser.getPhone());
        existing.setRole(updatedUser.getRole());

        return userRepository.save(existing);
    }

    // Delete user
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        // Validate old password
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Set new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}

