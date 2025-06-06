package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.exception.AppException;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 加解密工具
 *
 * @author CPoet
 */
public abstract class EncryptUtil {

    /** 获取私钥（建议环境变量配置） */
    public static final String RSA_PRIVATE_KEY = System.getProperty(AppConst.APP_NAME + ".PRIVATE_KEY", "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEArZrn15r+bFWRmsOlIQgFv0vbBgYWcR7FRTWTYeST+UZ0qmj8NtfOIoKskXYlfiZFkJBlC+MMz5Bxfyd2W4ujKwIDAQABAkEAg6KKmxxGQKTdY+lnY1Vu7i85Yyboi1uWnzTRjQ/B9+dnrCGkhLlnV2IZ79DC4ikagDbd9WTpGCATycNSpo0vMQIhAOx8KkUhBZgF4l8cY+FcKJtiMcBZPtP+od35+Iz8JvV5AiEAu+5g6y04YiIz/4i/OoKK9dYa23f3EBOs8ze/nmlx6MMCIQCpkR5+Ev9/0jhPGnxDy2ESiYZC9bsnJx3JnMOr0+HbgQIgM8A2Ai0GrMdcaE7A0hzmpUHbTXpgl5XDd4pvgvDohD8CIGcQWEwbFUAOgXBdvVoDe5uJqBHlpEA1FiTPJXCduCp5");
    /** 获取公钥（建议环境变量配置） */
    public static final String RSA_PUBLIC_KEY = System.getProperty(AppConst.APP_NAME + ".PUBLIC_KEY", "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAK2a59ea/mxVkZrDpSEIBb9L2wYGFnEexUU1k2Hkk/lGdKpo/DbXziKCrJF2JX4mRZCQZQvjDM+QcX8ndluLoysCAwEAAQ==");

    private EncryptUtil() {
    }

    public static String encryptWithRsaSys(String text) {
        PublicKey publicKeyWithRsa = getPublicKeyWithRsa(RSA_PUBLIC_KEY);
        return encryptWithRsa(text, publicKeyWithRsa);
    }

    public static PublicKey getPublicKeyWithRsa(String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new AppException("转换rsa公钥失败", e);
        }
    }

    public static String encryptWithRsa(String text, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new AppException("使用RSA加密失败", e);
        }
    }

    public static String decryptWithRsaSys(String text) {
        PrivateKey privateKeyWithRsa = getPrivateKeyWithRsa(RSA_PRIVATE_KEY);
        return decryptWithRsa(text, privateKeyWithRsa);
    }

    public static PrivateKey getPrivateKeyWithRsa(String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new AppException("转换rsa私钥失败", e);
        }
    }

    public static String decryptWithRsa(String text, PrivateKey privateKey) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(text);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new AppException("使用RSA解密失败", e);
        }
    }
}
