package com.example.Dr.VehicleCare.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(sess ->
            sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth -> auth

            // ✅ VERY IMPORTANT: allow OPTIONS (CORS preflight)
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            // ✅ Public APIs
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/uploads/**").permitAll()
            .requestMatchers("/api/bikes/**").permitAll()
            .requestMatchers("/api/services/**").permitAll()
            .requestMatchers("/api/chat/**").permitAll()

            // ✅ Razorpay / payment callbacks
            .requestMatchers("/api/payments/**").permitAll()

            // ✅ Booking APIs (JWT required)
            .requestMatchers("/api/bookings/**").authenticated()

            // ✅ Admin only
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // ✅ Provider only
            .requestMatchers("/api/provider/**").hasRole("PROVIDER")

            // ❌ everything else blocked
            .anyRequest().authenticated()
        );

    http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "https://www.drvehiclecare.com",
            "https://drvehiclecare.com"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}






