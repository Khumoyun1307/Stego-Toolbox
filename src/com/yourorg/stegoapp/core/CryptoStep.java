package com.yourorg.stegoapp.core;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Crypto step using AES encryption with password-based key derivation.
 * <p>
 * Uses PBKDF2WithHmacSHA256 for key derivation and AES/CBC/PKCS5Padding for encryption.
 * </p>
 */
public class CryptoStep implements StegoStep {
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    private final String password;

    /**
     * Constructs a CryptoStep with the given password.
     *
     * @param password The password to use for encryption/decryption
     */
    public CryptoStep(String password) {
        this.password = password;
    }

    /**
     * Encrypts the input string using AES and returns a Base64-encoded string containing salt, IV, and ciphertext.
     *
     * @param input The string to encrypt
     * @return Encrypted string in the format salt:iv:ciphertext (all Base64-encoded)
     */
    @Override
    public String encode(String input) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret, ivSpec);
            byte[] ciphertext = cipher.doFinal(input.getBytes("UTF-8"));
            String b64salt = Base64.getEncoder().encodeToString(salt);
            String b64iv   = Base64.getEncoder().encodeToString(iv);
            String b64ct   = Base64.getEncoder().encodeToString(ciphertext);
            return b64salt + ":" + b64iv + ":" + b64ct;
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    /**
     * Decrypts a string produced by encode().
     *
     * @param input Encrypted string in the format salt:iv:ciphertext
     * @return Decrypted original string
     * @throws IllegalArgumentException if the input format is invalid
     */
    @Override
    public String decode(String input) {
        // Pre-check format to allow IllegalArgumentException to propagate
        String[] parts = input.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted format");
        }
        try {
            byte[] salt       = Base64.getDecoder().decode(parts[0]);
            byte[] iv         = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
