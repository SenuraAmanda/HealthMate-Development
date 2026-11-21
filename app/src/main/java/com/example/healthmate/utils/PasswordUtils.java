package com.example.healthmate.utils;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Creates and checks salted PBKDF2 password hashes for local demo accounts. */
public class PasswordUtils {
    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;

    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = makeHash(password, salt, ITERATIONS);
            return "pbkdf2$" + ITERATIONS + "$"
                    + Base64.encodeToString(salt, Base64.NO_WRAP) + "$"
                    + Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Could not secure the password", e);
        }
    }

    public static boolean isHashed(String value) {
        return value != null && value.startsWith("pbkdf2$");
    }

    public static boolean verifyPassword(String password, String storedValue) {
        if (!isHashed(storedValue)) return false;
        try {
            String[] parts = storedValue.split("\\$");
            if (parts.length != 4) return false;
            int rounds = Integer.parseInt(parts[1]);
            byte[] salt = Base64.decode(parts[2], Base64.NO_WRAP);
            byte[] expected = Base64.decode(parts[3], Base64.NO_WRAP);
            byte[] actual = makeHash(password, salt, rounds);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] makeHash(String password, byte[] salt, int rounds) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
