package com.kineticfitness.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Salted password hashing: the raw password must never be stored, a correct
 * password must verify, and anything malformed must fail safely instead of throwing.
 */
class PasswordUtilTest {

    @Test
    @DisplayName("hash never contains the raw password")
    void hashDoesNotContainRawPassword() {
        String stored = PasswordUtil.hash("Secret123!");

        assertFalse(stored.contains("Secret123!"));
    }

    @Test
    @DisplayName("hash is stored as base64(salt):base64(hash)")
    void hashHasSaltAndHashParts() {
        String stored = PasswordUtil.hash("Secret123!");

        String[] parts = stored.split(":");
        assertEquals(2, parts.length);
        assertFalse(parts[0].isEmpty());
        assertFalse(parts[1].isEmpty());
    }

    @Test
    @DisplayName("the correct password verifies")
    void correctPasswordVerifies() {
        String stored = PasswordUtil.hash("Secret123!");

        assertTrue(PasswordUtil.verify("Secret123!", stored));
    }

    @Test
    @DisplayName("a wrong password does not verify")
    void wrongPasswordFails() {
        String stored = PasswordUtil.hash("Secret123!");

        assertFalse(PasswordUtil.verify("secret123!", stored));
        assertFalse(PasswordUtil.verify("", stored));
    }

    @Test
    @DisplayName("hashing the same password twice gives different strings (random salt) that both verify")
    void saltMakesHashesDiffer() {
        String first = PasswordUtil.hash("Secret123!");
        String second = PasswordUtil.hash("Secret123!");

        assertNotEquals(first, second);
        assertTrue(PasswordUtil.verify("Secret123!", first));
        assertTrue(PasswordUtil.verify("Secret123!", second));
    }

    @Test
    @DisplayName("an empty password can be hashed and verified")
    void emptyPasswordRoundTrip() {
        String stored = PasswordUtil.hash("");

        assertTrue(PasswordUtil.verify("", stored));
        assertFalse(PasswordUtil.verify("x", stored));
    }

    @Test
    @DisplayName("non-ASCII passwords round trip")
    void unicodePasswordRoundTrip() {
        String stored = PasswordUtil.hash("p\u00e4ssw\u00f6rd-\u5bc6\u7801");

        assertTrue(PasswordUtil.verify("p\u00e4ssw\u00f6rd-\u5bc6\u7801", stored));
        assertFalse(PasswordUtil.verify("password", stored));
    }

    @Test
    @DisplayName("verify returns false for a null password or null stored hash")
    void nullInputsFailSafely() {
        String stored = PasswordUtil.hash("Secret123!");

        assertFalse(PasswordUtil.verify(null, stored));
        assertFalse(PasswordUtil.verify("Secret123!", null));
    }

    @Test
    @DisplayName("verify returns false when the stored value has no separator")
    void missingSeparatorFails() {
        assertFalse(PasswordUtil.verify("Secret123!", "notahash"));
    }

    @Test
    @DisplayName("verify returns false (does not throw) when the stored value is not valid base64")
    void invalidBase64Fails() {
        assertFalse(PasswordUtil.verify("Secret123!", "!!!:???"));
    }
}