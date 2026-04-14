package com.chargeflow.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAuditLogger.class);

    public void registerSuccess(String email) {
        LOGGER.info("AUTH_REGISTER_SUCCESS email={}", email);
    }

    public void registerFailure(String email, String reason) {
        LOGGER.warn("AUTH_REGISTER_FAILURE email={} reason={}", email, reason);
    }

    public void loginSuccess(String email) {
        LOGGER.info("AUTH_LOGIN_SUCCESS email={}", email);
    }

    public void loginFailure(String email, String reason) {
        LOGGER.warn("AUTH_LOGIN_FAILURE email={} reason={}", email, reason);
    }
}

