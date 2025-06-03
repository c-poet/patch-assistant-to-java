package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.HashUtil;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 文件节点
 *
 * @author CPoet
 */
public class FileNode extends TreeKindNode {

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
        return bytes == null ? initFileBaseInfo() : bytes;
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

    protected byte[] initFileBaseInfo() {
        byte[] data = FileUtil.readFile(file);
        size = data.length;
        if (size > 10 * 1024 * 1024) {
            md5 = HashUtil.md5(data);
        } else {
            bytes = data;
        }
        return data;
    }
}
