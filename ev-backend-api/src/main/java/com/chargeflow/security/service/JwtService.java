package com.chargeflow.security.service;

import com.chargeflow.user.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(User user);
    String extractEmail(String token);
    boolean isTokenValid(String token, UserDetails userDetails);

}
