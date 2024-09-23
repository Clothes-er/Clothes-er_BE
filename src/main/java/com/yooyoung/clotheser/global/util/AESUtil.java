package com.yooyoung.clotheser.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AESUtil {

    @Value("${aes.key}")
    private String aesKey;

    private static final String ALGORITHM = "AES";
    private static final String ALGORITHM_MODE_PADDING = ALGORITHM + "/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 128;
    private static final int IV_SIZE = 16;

    // 한 번 사용함
    public String generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public String encrypt(String plainText) throws Exception {
        byte[] decodedKey = getBase64Decoded(aesKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, ALGORITHM);
        IvParameterSpec ivParameterSpec = createIv();

            Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] encryptedText = cipher.doFinal(plainText.getBytes());
            byte[] encryptedData = combineIvWithEncryptedText(ivParameterSpec, encryptedText);

        return Base64.getEncoder().encodeToString(encryptedData);
    }

    private byte[] getBase64Decoded(String data) {
        return Base64.getDecoder().decode(data);
    }

    private IvParameterSpec createIv() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    private byte[] combineIvWithEncryptedText(IvParameterSpec ivParameterSpec, byte[] encryptedText) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(IV_SIZE + encryptedText.length);
        byteBuffer.put(ivParameterSpec.getIV());
        byteBuffer.put(encryptedText);
        return byteBuffer.array();
    }

    public String decrypt(String encryptedData) throws Exception {
        byte[] decodedKey = getBase64Decoded(aesKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decodedKey, ALGORITHM);

        byte[] decodedEncryptedData = getBase64Decoded(encryptedData);
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedEncryptedData);

        byte[] iv = new byte[IV_SIZE];
        byteBuffer.get(iv);
        byte[] encryptedText = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedText);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] decryptedText = cipher.doFinal(encryptedText);
        return new String(decryptedText);
    }
}
