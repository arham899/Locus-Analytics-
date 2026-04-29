package com.locus.service.impl;

import com.locus.model.SystemAdministrator;
import com.locus.model.User;
import com.locus.service.AuthenticationService;
import com.locus.dao.UserDAO;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthenticationServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User login(String email, String password) {
        // TEMPORARY BYPASS FOR UI TESTING
        SystemAdministrator dummyUser = new SystemAdministrator();
        dummyUser.setUserId("admin-1");
        dummyUser.setName("Test Admin");

        this.currentUser = dummyUser;
        return dummyUser;
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