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
 * deliberately indistinguishable (unknown user vs. wrong password) to avoid user enumeration --
 * including in timing: an unknown username still pays the full PBKDF2 cost against a dummy hash,
 * so it takes as long to reject as a wrong password for a real account.
 */
public class AuthService {

    // Computed once against a fixed password so it always has a valid, current PasswordHasher
    // format; used only to keep the KDF cost identical for a username that doesn't exist.
    private static final String DUMMY_HASH = PasswordHasher.hash("timing-defense-dummy-password");

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return Optional.empty();
        }
        Optional<User> candidate = userDao.findByUsername(username.trim());
        String hashToVerify = candidate.map(User::getPasswordHash).orElse(DUMMY_HASH);
        boolean passwordMatches = PasswordHasher.verify(password, hashToVerify);
        return candidate.filter(user -> passwordMatches);
    }
}
