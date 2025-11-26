package cn.cpoet.patch.assistant.control.tree.node;

import cn.cpoet.patch.assistant.common.InputBufConsumer;
import cn.cpoet.patch.assistant.common.InputStreamConsumer;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.TreeNodeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 文件节点
 *
 * @author CPoet
 */
public class FileNode extends TreeNode {

    /**
     * 关联的文件
     */
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void consumeBytes(InputBufConsumer consumer) {
        TreeNodeUtil.readNodeFile(this, consumer);
    }

    @Override
    public void consumeInputStream(InputStreamConsumer consumer) {
        try (InputStream in = new FileInputStream(file)) {
            consumer.accept(in);
        } catch (IOException e) {
            throw new AppException("Failed to read file contents", e);
        }
    }

    @Override
    public boolean isDir() {
        return file.isDirectory();
    }

    @Override
    public LocalDateTime getModifyTime() {
        long lastModified = file.lastModified();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
    }
}
