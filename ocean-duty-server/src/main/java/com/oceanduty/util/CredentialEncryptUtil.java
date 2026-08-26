package com.oceanduty.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 敏感凭据 AES-GCM 加解密工具
 */
@Component
public class CredentialEncryptUtil {

    private static final String PREFIX = "ENC:";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKeySpec secretKey;

    public CredentialEncryptUtil(
            @Value("${ocean-duty.security.encrypt-secret:${ocean-duty.jwt.secret:ocean-duty-default-secret-change-in-production}}")
            String secret) {
        this.secretKey = new SecretKeySpec(deriveKey(secret), "AES");
    }

    /**
     * 加密明文，返回 ENC: 前缀密文
     */
    public String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText) || isEncrypted(plainText)) {
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("凭据加密失败", e);
        }
    }

    /**
     * 解密密文；非 ENC 前缀则原样返回（兼容迁移前数据）
     */
    public String decrypt(String cipherText) {
        if (!StringUtils.hasText(cipherText) || !isEncrypted(cipherText)) {
            return cipherText;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(cipherText.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plain = cipher.doFinal(encrypted);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("凭据解密失败", e);
        }
    }

    public boolean isEncrypted(String value) {
        return StringUtils.hasText(value) && value.startsWith(PREFIX);
    }

    private byte[] deriveKey(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("加密密钥派生失败", e);
        }
    }
}
