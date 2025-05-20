package cn.cpoet.patch.assistant.view.tree;

/**
 * 文件树形节点
 *
 * @author CPoet
 */
public class FileNode extends TreeNode {

    /**
     * 路径
     */
    private String path;

    /**
     * 内容
     */
    private byte[] bytes;

    /**
     * 内容md5值
     */
    private String md5;

    /**
     * 文件状态
     */
    private FileNodeStatus status = FileNodeStatus.NONE;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public FileNodeStatus getStatus() {
        return status;
    }

    public void setStatus(FileNodeStatus status) {
        this.status = status;
    }
}
