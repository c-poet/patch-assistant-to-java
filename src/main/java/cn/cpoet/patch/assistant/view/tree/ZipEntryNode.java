package cn.cpoet.patch.assistant.view.tree;

import java.util.zip.ZipEntry;

/**
 * @author CPoet
 */
public class ZipEntryNode extends FileNode {

    private ZipEntry entry;

    public ZipEntry getEntry() {
        return entry;
    }

    public void setEntry(ZipEntry entry) {
        this.entry = entry;
    }
}
