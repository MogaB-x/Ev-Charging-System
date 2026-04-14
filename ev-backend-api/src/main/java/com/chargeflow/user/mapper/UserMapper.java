package com.chargeflow.user.mapper;

import com.chargeflow.user.dto.AccountDetailsResponse;
import com.chargeflow.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public AccountDetailsResponse toAccountDetailsResponse(User user) {
        return new AccountDetailsResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isEnabled(),
                user.getBalance()
        );
    }
}

