package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.HashUtil;

import java.io.*;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;

/**
 * 压缩节点工厂
 *
 * @author CPoet
 */
public abstract class CompressNodeFactory<E> {
    /**
     * 创建节点
     *
     * @param entry 压缩信息
     * @param in    输入流
     * @return 节点
     */
    public abstract CompressNode create(E entry, InputStream in);

    protected void creteNodeFile(CompressNode node, InputStream in) {
        File tempDir = AppContext.getInstance().getTempDir();
        String fileName = FileNameUtil.uniqueFileName(node.getName());
        File file = new File(tempDir, fileName);
        MessageDigest md5Digest = HashUtil.createMd5Digest();
        try (OutputStream out = new FileOutputStream(file)) {
            FileUtil.readBuf(in, (len, buf) -> {
                md5Digest.update(buf, 0, len);
                out.write(buf, 0, len);
            });
        } catch (IOException e) {
            throw new AppException("Writing node cache file failed", e);
        }
        node.setMd5(HashUtil.toHexStr(md5Digest));
        node.setFile(file);
    }

    public static <E> CompressNodeFactory<E> getInstance(E entry) {
        return getInstance(entry.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <E> CompressNodeFactory<E> getInstance(Class<?> entryClass) {
        if (ZipEntry.class.isAssignableFrom(entryClass)) {
            return (CompressNodeFactory<E>) CompressNodeZipFactory.INSTANCE;
        }
        if (LocalFileInfo.class.isAssignableFrom(entryClass)) {
            return (CompressNodeFactory<E>) CompressNodeFileFactory.INSTANCE;
        }
        throw new AppException("Unsupported compression type");
    }
}
