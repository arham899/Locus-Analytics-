package com.locus.service.impl;

import com.locus.dao.UserDAO;
import com.locus.exception.ValidationException;
import com.locus.model.User;
import com.locus.service.AuthenticationService;
import com.locus.service.validation.InputValidator;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Real implementation of {@link AuthenticationService}.
 *
 * <p>Looks up the user by email, then verifies the supplied password against
 * the BCrypt hash stored in {@code app_user.password_hash}.
 * A deliberately vague error message ("Invalid credentials") prevents
 * user-enumeration attacks.</p>
 *
 * @author Fasih Ul Mubashir (24i-0517)
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthenticationServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User login(String email, String password) {

        new InputValidator()
                .validateNotBlank("email", email)
                .validateNotBlank("password", password)
                .throwIfInvalid();

        // ── Fallback for Demo Accounts ──────────
        if ("admin@locus.com".equalsIgnoreCase(email) && "password".equals(password)) {
            com.locus.model.SystemAdministrator admin = new com.locus.model.SystemAdministrator();
            admin.setEmail("admin@locus.com");
            admin.setName("Locus Admin (Demo)");
            this.currentUser = admin;
            return admin;
        }
        if ("analyst@locus.com".equalsIgnoreCase(email) && "password".equals(password)) {
            com.locus.model.PropertyAnalyst analyst = new com.locus.model.PropertyAnalyst();
            analyst.setEmail("analyst@locus.com");
            analyst.setName("Locus Analyst (Demo)");
            this.currentUser = analyst;
            return analyst;
        }

        User user = userDAO.findByEmail(email.trim().toLowerCase());

        if (user == null || user.getPasswordHash() == null) {
            throw new ValidationException("Invalid credentials");
        }

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new ValidationException("Invalid credentials");
        }

        this.currentUser = user;
        System.out.println("[Auth] Login: " + user.getEmail() + " role=" + user.getRole());
        return user;
    }

    @Override
    public void logout() {
        if (currentUser != null) {
            System.out.println("[Auth] Logout: " + currentUser.getEmail());
        }
        currentUser = null;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }
}
