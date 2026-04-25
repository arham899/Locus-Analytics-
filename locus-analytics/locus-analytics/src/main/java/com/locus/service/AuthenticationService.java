package com.locus.service;

import com.locus.model.User;

/**
 * Service for user authentication and session management.
 *
 * <p>Authenticates users against the app_user table and maintains
 * the current session. Role-based menu loading depends on the returned User type.</p>
 */
public interface AuthenticationService {

    /**
     * Authenticates a user by email and password.
     *
     * @param email    user email
     * @param password user password
     * @return the authenticated User (PropertyAnalyst or SystemAdministrator)
     * @throws com.locus.exception.ValidationException if credentials are invalid
     */
    User login(String email, String password);

    /**
     * Logs out the current user and clears the session.
     */
    void logout();

    /**
     * Returns the currently authenticated user, or null if not logged in.
     *
     * @return current User, or null
     */
    User getCurrentUser();
}
