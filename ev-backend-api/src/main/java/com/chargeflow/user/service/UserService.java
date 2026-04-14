package com.chargeflow.user.service;

import com.chargeflow.user.dto.AccountDetailsResponse;

public interface UserService {
    AccountDetailsResponse getCurrentAccountDetails();

    AccountDetailsResponse updatePhoneNumber(String phoneNumber);
}
