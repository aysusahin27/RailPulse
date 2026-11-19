package com.basespringboot.security;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordService {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String hashPassword(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hash = hashWithSalt(password, saltBase64);
        return saltBase64 + ":" + hash;
    }

    public boolean verifyPassword(String rawPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        String saltBase64 = parts[0];
        String expectedHash = parts[1];
        String computedHash = hashWithSalt(rawPassword, saltBase64);
        return MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.UTF_8), computedHash.getBytes(StandardCharsets.UTF_8));
    }

    private String hashWithSalt(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
