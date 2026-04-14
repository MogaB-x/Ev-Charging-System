package com.chargeflow.user.controller;

import com.chargeflow.user.dto.AccountDetailsResponse;
import com.chargeflow.user.dto.UpdatePhoneNumberRequest;
import com.chargeflow.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public AccountDetailsResponse getAccountDetails() {
        return userService.getCurrentAccountDetails();
    }

    @PatchMapping("/phone-number")
    public AccountDetailsResponse updatePhoneNumber(@RequestBody @Valid UpdatePhoneNumberRequest request) {
        return userService.updatePhoneNumber(request.phoneNumber());
    }
}
