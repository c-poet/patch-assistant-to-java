package cn.cpoet.patch.assistant.view.tree;

/**
 * @author CPoet
 */
public class FileNode extends TreeNode {

    private String path;

    private byte[] bytes;

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
}
