package com.example.Dr.VehicleCare.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

   @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    String path = request.getRequestURI();
    String method = request.getMethod();

    // Allow preflight requests
    if ("OPTIONS".equalsIgnoreCase(method)) {
        response.setStatus(HttpServletResponse.SC_OK);
        return;
    }

    // Skip JWT check for public endpoints
    if (path.startsWith("/auth/")
            || path.startsWith("/uploads/")
            || path.startsWith("/api/bikes")
            || path.startsWith("/api/services")
            || path.startsWith("/api/users")
            || path.startsWith("/api/customized")
            || path.startsWith("/api/bikes/companies")) {
        filterChain.doFilter(request, response);
        return;
    }

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Missing or invalid Authorization header");
        return;
    }

    String token = authHeader.substring(7);

    if (!jwtProvider.validateToken(token)) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Invalid or expired JWT token");
        return;
    }

    Claims claims = jwtProvider.getClaims(token);
    String userId = claims.getSubject();
    String role = claims.get("role", String.class);

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
}
}

