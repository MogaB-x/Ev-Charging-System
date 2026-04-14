package com.chargeflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "Invalid email")
        @NotBlank(message = "Email is mandatory")
        String email,

        @NotBlank(message = "Password is mandatory")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @NotBlank(message = "First name is mandatory")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is mandatory")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "Phone number is mandatory")
        @Size(max = 30, message = "Phone number must be at most 30 characters long")
        @Pattern(regexp = "^[0-9+()\\-\\s]{7,30}$", message = "Invalid phone number format")
        String phoneNumber
) {}
