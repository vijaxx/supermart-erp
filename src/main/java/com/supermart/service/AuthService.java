package com.supermart.service;

import com.supermart.dao.UserDao;
import com.supermart.model.User;
import com.supermart.security.PasswordHasher;

import java.util.Optional;

/**
 * Authentication business rules.
 *
 * <p>The lookup is a parameterised query and the password is checked against a salted PBKDF2 hash,
 * so neither the username nor the password field can influence the SQL that runs. Failures are
 * deliberately indistinguishable (unknown user vs. wrong password) to avoid user enumeration.
 */
public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> candidate = userDao.findByUsername(username.trim());
        if (candidate.isEmpty()) {
            return Optional.empty();
        }
        User user = candidate.get();
        return PasswordHasher.verify(password, user.getPasswordHash()) ? Optional.of(user) : Optional.empty();
    }
}
