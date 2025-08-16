package cn.cpoet.patch.assistant.control.tree.node;

import java.time.LocalDateTime;
import java.util.zip.ZipEntry;

/**
 * @author CPoet
 */
public class ZipEntryNode extends TreeNode {

    private ZipEntry entry;

    public ZipEntry getEntry() {
        return entry;
    }

    public void setEntry(ZipEntry entry) {
        this.entry = entry;
    }

    @Override
    public boolean isDir() {
        return entry.isDirectory();
    }

    @Override
    public LocalDateTime getModifyTime() {
        return entry.getTimeLocal();
    }
}
