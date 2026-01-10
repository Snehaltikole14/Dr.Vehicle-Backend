package com.example.Dr.VehicleCare.dto;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class SignUpRequest {
    @NotBlank private String name;

    @NotBlank private String password;
    private String phone;
    private String role;
    @NotBlank
    private String otp;// optional, default CUSTOMER
}

