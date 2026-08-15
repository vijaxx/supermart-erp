package com.supermart.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void correctPasswordVerifiesSuccessfully() {
        String hash = PasswordHasher.hash("Correct-Horse-9");
        assertTrue(PasswordHasher.verify("Correct-Horse-9", hash));
    }

    @Test
    void wrongPasswordFailsVerification() {
        String hash = PasswordHasher.hash("Correct-Horse-9");
        assertFalse(PasswordHasher.verify("wrong-password", hash));
    }

    @Test
    void hashIsNeverStoredAsPlaintext() {
        String plain = "Admin@123";
        String hash = PasswordHasher.hash(plain);
        assertNotEquals(plain, hash);
        assertTrue(hash.startsWith("pbkdf2$"));
    }

    @Test
    void sameRawPasswordProducesDifferentHashesBecauseOfRandomSalt() {
        String hashOne = PasswordHasher.hash("SamePassword1");
        String hashTwo = PasswordHasher.hash("SamePassword1");
        assertNotEquals(hashOne, hashTwo, "each hash must use a fresh random salt");
        assertTrue(PasswordHasher.verify("SamePassword1", hashOne));
        assertTrue(PasswordHasher.verify("SamePassword1", hashTwo));
    }

    @Test
    void malformedStoredHashFailsClosed() {
        assertFalse(PasswordHasher.verify("anything", "not-a-real-hash"));
        assertFalse(PasswordHasher.verify("anything", ""));
    }

    @Test
    void emptyPasswordCannotBeHashed() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
    }

    @Test
    void nullInputsFailVerificationRatherThanThrow() {
        assertFalse(PasswordHasher.verify(null, "pbkdf2$1$AA$AA"));
        assertFalse(PasswordHasher.verify("pw", null));
    }
}
