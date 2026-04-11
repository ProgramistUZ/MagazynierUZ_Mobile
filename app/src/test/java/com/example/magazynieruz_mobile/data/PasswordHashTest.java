package com.example.magazynieruz_mobile.data;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordHashTest {

    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Test
    public void hashPassword_sameInput_sameOutput() throws Exception {
        String hash1 = hashPassword("testPassword123");
        String hash2 = hashPassword("testPassword123");
        assertEquals(hash1, hash2);
    }

    @Test
    public void hashPassword_differentInput_differentOutput() throws Exception {
        String hash1 = hashPassword("password1");
        String hash2 = hashPassword("password2");
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void hashPassword_returns64CharHex() throws Exception {
        String hash = hashPassword("anything");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }
}
