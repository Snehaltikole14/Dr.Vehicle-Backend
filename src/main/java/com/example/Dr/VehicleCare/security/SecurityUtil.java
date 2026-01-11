package com.example.Dr.VehicleCare.security;

import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtil {

    @Autowired
    private UserService userService;

    /**
     * Get currently authenticated user from JWT
     * JWT subject = userId
     */
    public Optional<User> getCurrentUser() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }

        try {
            Long userId = Long.parseLong(auth.getName());
            return userService.findById(userId);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Get current user ID
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }
}
