package com.chargeflow.auth.mapper;

import com.chargeflow.auth.dto.RegisterRequest;
import com.chargeflow.user.entity.User;
import com.chargeflow.user.entity.UserRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AuthMapper {

    public User toUser(RegisterRequest request, String encodedPassword) {
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setBalance(BigDecimal.ZERO);
        return user;
    }
}

