package com.bolaihui.weight.gui.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by fz on 2015/11/18.
 */
public class AES128Util {

    private static final String DEFAULT_CHARSET = "utf-8";

    private static final String ALGORITHM = "AES";

    private static final String TRANSFORMATION = "AES";

    private static final int BITS = 128;

    public static String encrypt(String base64KeyWord, String content) {
        try {
            SecretKeySpec key = getAESSecretKey(base64KeyWord);
            byte[] byteContent = content.getBytes(DEFAULT_CHARSET);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(byteContent);
            byte[] encode = Base64.encodeBase64(result);
            return new String(encode, DEFAULT_CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("AES加密出错", e);
        }
    }

    public static String decrypt(String base64KeyWord, String content) {
        try {
            SecretKeySpec key = getAESSecretKey(base64KeyWord);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(Base64.decodeBase64(content));
            return new String(result, DEFAULT_CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("AES解密出错", e);
        }
    }

    public static SecretKeySpec getAESSecretKey(String base64KeyWord) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(BITS, new SecureRandom(Base64.decodeBase64(base64KeyWord)));
        SecretKey secretKey = keyGen.generateKey();
        SecretKeySpec key = new SecretKeySpec(secretKey.getEncoded(), ALGORITHM);
        return key;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {

        String keyWord = "today_is_a_sunny_day";
        byte[] base64KeyBytes = Base64.encodeBase64(keyWord.getBytes(DEFAULT_CHARSET));
        String base64KeyWord = new String(base64KeyBytes, DEFAULT_CHARSET);
        String content = "122qw";

        System.out.println("加密密钥：" + base64KeyWord);
        System.out.println("加密前内容：" + content);

        String encryptContent = encrypt(base64KeyWord, content);
        String decryptContent = decrypt(base64KeyWord, encryptContent);

        System.out.println("加密后内容：" + encryptContent);
        System.out.println("解密后内容：" + decryptContent);
    }
}
