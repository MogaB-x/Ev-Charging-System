package com.chargeflow.user.service;

import com.chargeflow.common.exception.NotFoundException;
import com.chargeflow.common.exception.UnauthorizedException;
import com.chargeflow.user.dto.AccountDetailsResponse;
import com.chargeflow.user.entity.User;
import com.chargeflow.user.mapper.UserMapper;
import com.chargeflow.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public AccountDetailsResponse getCurrentAccountDetails() {
        User user = getAuthenticatedUser();
        return userMapper.toAccountDetailsResponse(user);
    }

    @Override
    public AccountDetailsResponse updatePhoneNumber(String phoneNumber) {
        User user = getAuthenticatedUser();
        user.setPhoneNumber(phoneNumber);
        User savedUser = userRepository.save(user);
        return userMapper.toAccountDetailsResponse(savedUser);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Authentication is required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
    }
}
