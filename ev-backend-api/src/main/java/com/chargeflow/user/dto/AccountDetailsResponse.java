package com.chargeflow.user.dto;

import com.chargeflow.user.entity.UserRole;

import java.math.BigDecimal;

public record AccountDetailsResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        UserRole role,
        boolean enabled,
        BigDecimal balance
) {
}

