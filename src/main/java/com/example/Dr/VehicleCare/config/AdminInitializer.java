package com.example.Dr.VehicleCare.config;

import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.UserRole;
import com.example.Dr.VehicleCare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void createAdmin() {
        try {
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setName("Super Admin");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
                admin.setRole(UserRole.ADMIN);

                userRepository.save(admin);
                System.out.println("✅ Admin user created");
            } else {
                System.out.println("⚠ Admin already exists");
            }
        } catch (Exception e) {
            System.err.println("⚠ Database not ready yet, skipping admin creation");
        }
    }
}
