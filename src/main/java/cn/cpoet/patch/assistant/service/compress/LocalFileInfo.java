package cn.cpoet.patch.assistant.service.compress;

import java.io.File;

/**
 * 文件信息
 *
 * @author CPoet
 */
public class LocalFileInfo {
    /**
     * 解压后的文件
     */
    private File file;

    /**
     * 路径
     */
    private String path;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
