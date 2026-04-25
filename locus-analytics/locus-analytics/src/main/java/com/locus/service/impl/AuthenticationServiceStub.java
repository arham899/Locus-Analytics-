package com.locus.service.impl;

import com.locus.exception.ValidationException;
import com.locus.model.PropertyAnalyst;
import com.locus.model.SystemAdministrator;
import com.locus.model.User;
import com.locus.service.AuthenticationService;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub implementation of {@link AuthenticationService}.
 * Hardcodes "admin@locus.com" and "analyst@locus.com" with password "password".
 */
public class AuthenticationServiceStub implements AuthenticationService {

    private User currentUser;

    @Override
    public User login(String email, String password) {
        if (!"password".equals(password)) {
            Map<String, String> errors = new HashMap<>();
            errors.put("password", "Invalid password");
            throw new ValidationException("Authentication failed", errors);
        }

        if ("admin@locus.com".equalsIgnoreCase(email)) {
            currentUser = new SystemAdministrator("admin-1", "Admin Fasih", email, "SuperAdmin");
            return currentUser;
        } else if ("analyst@locus.com".equalsIgnoreCase(email)) {
            currentUser = new PropertyAnalyst("analyst-1", "Analyst Arham", email, "Level 3");
            return currentUser;
        }

        Map<String, String> errors = new HashMap<>();
        errors.put("email", "User not found");
        throw new ValidationException("Authentication failed", errors);
    }

    @Override
    public void logout() {
        this.currentUser = null;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }
}
