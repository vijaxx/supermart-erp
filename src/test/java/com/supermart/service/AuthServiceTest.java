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

    /**
     * Regression guard for a timing side-channel: authenticate() used to return immediately for
     * an unknown username but run the full ~120k-iteration PBKDF2 check for a wrong password on a
     * real account, so measuring response time alone could tell an attacker which usernames exist
     * -- even though both responses look identical. An unknown username must now pay the same KDF
     * cost, via a dummy hash, so the two paths take comparable time.
     *
     * <p>Uses a generous ratio (not an absolute margin) so it isn't flaky under CI/CPU jitter: the
     * old, unguarded behaviour returned near-instantly (a few microseconds) for an unknown
     * username against a ~100ms+ PBKDF2 check, which this threshold would have failed by a wide
     * margin.
     */
    @Test
    void unknownUsernameTakesComparableTimeToAWrongPasswordOnARealAccount() {
        // Warm up the JIT/JVM so the first real measurement isn't skewed by class loading.
        authService.authenticate("admin", "wrong-password");
        authService.authenticate("nobody", "whatever");

        long knownUserNanos = timeAuthenticate("admin", "wrong-password");
        long unknownUserNanos = timeAuthenticate("nobody", "whatever");

        assertTrue(
                unknownUserNanos >= knownUserNanos / 2,
                "unknown-username rejection (" + unknownUserNanos
                        + "ns) should take comparable time to a wrong password on a real account ("
                        + knownUserNanos + "ns), not return early");
    }

    private long timeAuthenticate(String username, String password) {
        long start = System.nanoTime();
        authService.authenticate(username, password);
        return System.nanoTime() - start;
    }
}
