package com.chargeflow.auth.service;

import com.chargeflow.auth.dto.AuthResponse;
import com.chargeflow.auth.dto.LoginRequest;
import com.chargeflow.auth.dto.RegisterRequest;
import com.chargeflow.auth.mapper.AuthMapper;
import com.chargeflow.common.exception.ConflictException;
import com.chargeflow.common.exception.UnauthorizedException;
import com.chargeflow.logger.AuthAuditLogger;
import com.chargeflow.security.service.JwtService;
import com.chargeflow.user.entity.User;
import com.chargeflow.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthAuditLogger authAuditLogger;
    private final AuthMapper authMapper;


    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            authAuditLogger.registerFailure(request.email(), "email already in use");
            throw new ConflictException("Email is already in use");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = authMapper.toUser(request, encodedPassword);

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        authAuditLogger.registerSuccess(user.getEmail());

        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException ex) {
            authAuditLogger.loginFailure(request.email(), "invalid credentials");
            throw new UnauthorizedException("Invalid credentials");
        } catch (DisabledException ex) {
            authAuditLogger.loginFailure(request.email(), "user disabled");
            throw new UnauthorizedException("User account is disabled");
        } catch (AuthenticationException ex) {
            authAuditLogger.loginFailure(request.email(), "authentication failed");
            throw new UnauthorizedException("Authentication failed");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        String token = jwtService.generateToken(user);
        authAuditLogger.loginSuccess(user.getEmail());

        return new AuthResponse(token);
    }
}
