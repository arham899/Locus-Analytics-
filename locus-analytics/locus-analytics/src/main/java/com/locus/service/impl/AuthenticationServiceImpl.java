package com.locus.service.impl;

import com.locus.exception.ValidationException;
import com.locus.model.User;
import com.locus.service.AuthenticationService;
import com.locus.dao.UserDAO;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthenticationServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User login(String email, String password) {

        User user = userDAO.findByEmail(email);

        // STEP 1: check user exists
        if (user == null) {
            throw new ValidationException("Authentication failed",
                    Map.of("email", "User not found"));
        }

        // STEP 2: check password (BCrypt compare)
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new ValidationException("Authentication failed",
                    Map.of("password", "Invalid password"));
        }

        // STEP 3: set session
        currentUser = user;
        return user;
    }

    @Override
    public void logout() {
        currentUser = null;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }
}