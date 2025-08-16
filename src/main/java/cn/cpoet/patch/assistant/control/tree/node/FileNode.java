package cn.cpoet.patch.assistant.control.tree.node;

import cn.cpoet.patch.assistant.util.TreeNodeUtil;

import java.io.File;
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
    public byte[] getBytes() {
        return TreeNodeUtil.readNodeFile(this, file);
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
