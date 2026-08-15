package com.supermart.service;

import com.supermart.TestDatabases;
import com.supermart.config.Database;
import com.supermart.dao.UserDao;
import com.supermart.model.Role;
import com.supermart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Authentication behaviour, including the SQL-injection regression test called out in the README.
 * Every DAO query is a {@code PreparedStatement}, so an injected quote is bound as literal data
 * rather than altered SQL, and login correctly fails.
 */
class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        Database database = TestDatabases.fresh();
        authService = new AuthService(new UserDao(database));
    }

    @Test
    void correctAdminCredentialsAuthenticate() {
        Optional<User> user = authService.authenticate("admin", "Admin@123");
        assertTrue(user.isPresent());
        assertEquals(Role.ADMIN, user.get().getRole());
    }

    @Test
    void correctStaffCredentialsAuthenticate() {
        Optional<User> user = authService.authenticate("staff", "Staff@123");
        assertTrue(user.isPresent());
        assertEquals(Role.STAFF, user.get().getRole());
    }

    @Test
    void wrongPasswordIsRejected() {
        assertTrue(authService.authenticate("admin", "wrong-password").isEmpty());
    }

    @Test
    void unknownUsernameIsRejected() {
        assertTrue(authService.authenticate("nobody", "whatever").isEmpty());
    }

    /**
     * THE injection test: a classic tautology payload in the username field must not authenticate.
     * If the DAO ever concatenated SQL instead of using a PreparedStatement, a query like
     * {@code WHERE username = '' OR '1'='1'} would return the first row in the table and this
     * test would fail.
     */
    @Test
    void classicSqlInjectionPayloadInUsernameDoesNotAuthenticate() {
        assertTrue(authService.authenticate("' OR '1'='1", "anything").isEmpty());
    }

    @Test
    void sqlInjectionPayloadInPasswordDoesNotAuthenticate() {
        assertTrue(authService.authenticate("admin", "' OR '1'='1").isEmpty());
    }

    @Test
    void commentBasedInjectionPayloadDoesNotAuthenticate() {
        assertTrue(authService.authenticate("admin' -- ", "irrelevant").isEmpty());
    }

    @Test
    void unionBasedInjectionPayloadDoesNotAuthenticate() {
        assertTrue(authService.authenticate("' UNION SELECT 1,'x','x','x','ADMIN' -- ", "irrelevant").isEmpty());
    }

    @Test
    void blankCredentialsAreRejectedWithoutHittingTheDatabase() {
        assertTrue(authService.authenticate("", "").isEmpty());
        assertTrue(authService.authenticate(null, null).isEmpty());
    }
}
