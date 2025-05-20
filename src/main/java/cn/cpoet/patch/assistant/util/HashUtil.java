package cn.cpoet.patch.assistant.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author CPoet
 */
public abstract class HashUtil {

    private HashUtil() {
    }

    /**
     * 字节数组转16进制
     *
     * @param bytes 字节数组
     * @return 转换结果
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 计算md5值
     *
     * @param bytes 字节数组
     * @return md5值
     */
    public static String md5(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(bytes);
            return toHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
