package com.example.Dr.VehicleCare.config;

import com.example.Dr.VehicleCare.model.User;
import com.example.Dr.VehicleCare.model.enums.UserRole;
import com.example.Dr.VehicleCare.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminConfig {

    @Bean
    CommandLineRunner createAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setName("Super Admin");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(passwordEncoder.encode("Admin@123")); // strong password
                admin.setRole(UserRole.ADMIN);

                userRepository.save(admin);
                System.out.println("✅ Admin user created: admin@example.com / Admin@123");
            } else {
                System.out.println("⚠ Admin already exists");
            }
        };
    }
}
