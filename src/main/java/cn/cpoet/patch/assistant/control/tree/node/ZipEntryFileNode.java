package cn.cpoet.patch.assistant.control.tree.node;

import cn.cpoet.patch.assistant.util.TreeNodeUtil;

import java.io.File;

/**
 * 压缩节点（创建临时文件）
 *
 * @author CPoet
 */
public class ZipEntryFileNode extends ZipEntryNode {

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
}
