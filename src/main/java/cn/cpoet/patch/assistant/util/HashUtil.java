package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.model.HashInfo;

import java.io.IOException;
import java.io.InputStream;
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
    public static String toHexStr(byte[] bytes) {
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
     * 字节数组转16进制
     *
     * @param digest 计算结果
     * @return 转换结果
     */
    public static String toHexStr(MessageDigest digest) {
        return toHexStr(digest.digest());
    }

    public static MessageDigest createMd5Digest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new AppException("The MD5 algorithm is not supported", e);
        }
    }

    public static MessageDigest createSha1Digest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new AppException("The SHA1 algorithm is not supported", e);
        }
    }

    /**
     * 计算md5值
     *
     * @param bytes 字节数组
     * @return md5值
     */
    public static String md5(byte[] bytes) {
        byte[] digest = createMd5Digest().digest(bytes);
        return toHexStr(digest);
    }

    /**
     * 计算sha1值
     *
     * @param bytes 字节数组
     * @return sha1值
     */
    public static String sha1(byte[] bytes) {
        byte[] digest = createSha1Digest().digest(bytes);
        return toHexStr(digest);
    }

    /**
     * 获取完整hash信息
     *
     * @param in 输入流
     * @return 完整hash信息
     * @throws IOException IO异常信息
     */
    public static HashInfo getHashInfo(InputStream in) throws IOException {
        HashInfo hashInfo = new HashInfo();
        MessageDigest md5Digest = createMd5Digest();
        MessageDigest sha1Digest = createSha1Digest();
        FileUtil.readBuf(in, (len, buf) -> {
            md5Digest.update(buf, 0, len);
            sha1Digest.update(buf, 0, len);
            hashInfo.setLength(hashInfo.getLength() + len);
        });
        hashInfo.setMd5(toHexStr(md5Digest));
        hashInfo.setSha1(toHexStr(sha1Digest));
        return hashInfo;
    }
}
