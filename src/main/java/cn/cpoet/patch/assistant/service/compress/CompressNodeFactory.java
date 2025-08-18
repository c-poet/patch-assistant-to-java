package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.HashUtil;
import com.github.junrar.rarfile.FileHeader;

import java.io.File;
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
     * @param bytes 数据
     * @return 节点
     */
    public abstract CompressNode create(E entry, byte[] bytes);

    protected void creteNodeFile(CompressNode node, byte[] bytes) {
        File tempDir = AppContext.getInstance().getTempDir();
        String fileName = FileNameUtil.uniqueFileName(node.getName());
        File file = new File(tempDir, fileName);
        FileUtil.writeFile(new File(tempDir, fileName), bytes);
        node.setFile(file);
    }

    protected void createNodeFileAndHash(CompressNode node, byte[] bytes) {
        node.setMd5(HashUtil.md5(bytes));
        creteNodeFile(node, bytes);
    }

    public static <E> CompressNodeFactory<E> getInstance(E entry) {
        return getInstance(entry.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <E> CompressNodeFactory<E> getInstance(Class<?> entryClass) {
        if (FileHeader.class.isAssignableFrom(entryClass)) {
            return (CompressNodeFactory<E>) CompressNodeRarFactory.INSTANCE;
        }
        if (ZipEntry.class.isAssignableFrom(entryClass)) {
            return (CompressNodeFactory<E>) CompressNodeZipFactory.INSTANCE;
        }
        if (LocalFileInfo.class.isAssignableFrom(entryClass)) {
            return (CompressNodeFactory<E>) CompressNodeFileFactory.INSTANCE;
        }
        throw new AppException("Unsupported compression type");
    }
}
