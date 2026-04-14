package com.chargeflow.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePhoneNumberRequest(
        @NotBlank(message = "Phone number is mandatory")
        @Size(max = 30, message = "Phone number must be at most 30 characters long")
        @Pattern(regexp = "^[0-9+()\\-\\s]{7,30}$", message = "Invalid phone number format")
        String phoneNumber
) {
}

