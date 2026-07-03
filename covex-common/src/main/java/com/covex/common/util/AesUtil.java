package com.covex.common.util;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * AES 加密工具 — 用于证件号、手机号、银行账号等敏感字段加密存储
 * 使用 AES/ECB/PKCS5Padding 以实现确定性加密（相同明文 → 相同密文，支持加密后比对查询）
 */
@Component
public class AesUtil {

    private static final Logger log = LoggerFactory.getLogger(AesUtil.class);
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private final SecretKeySpec secretKey;

    public AesUtil(@Value("${covex.aes.key}") String key) {
        try {
            // Derive a 128-bit key from the config value using SHA-256 truncation
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            byte[] aes128Key = new byte[16];
            System.arraycopy(keyBytes, 0, aes128Key, 0, 16);
            this.secretKey = new SecretKeySpec(aes128Key, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AES key", e);
        }
    }

    /**
     * 加密明文 → 返回 Hex 编码的密文
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(encrypted);
        } catch (Exception e) {
            log.error("AES encryption failed", e);
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    /**
     * 解密 Hex 编码的密文 → 返回明文
     */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Hex.decodeHex(cipherText.toCharArray());
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES decryption failed", e);
            throw new RuntimeException("AES decryption failed", e);
        }
    }
}
