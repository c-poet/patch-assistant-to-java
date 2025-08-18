package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.util.DateUtil;
import cn.cpoet.patch.assistant.util.FileNameUtil;

import java.util.zip.ZipEntry;

/**
 * @author CPoet
 */
public class CompressNodeZipFactory extends CompressNodeFactory<ZipEntry> {

    public static final CompressNodeZipFactory INSTANCE = new CompressNodeZipFactory();

    @Override
    public CompressNode create(ZipEntry entry, byte[] bytes) {
        CompressNode node = new CompressNode();
        node.setName(FileNameUtil.getFileName(entry.getName()));
        node.setText(node.getName());
        node.setPath(entry.getName());
        node.setExtra(entry.getExtra());
        node.setCreateTime(DateUtil.toLocalDateTime(entry.getCreationTime()));
        node.setModifyTime(DateUtil.toLocalDateTime(entry.getLastModifiedTime()));
        node.setAccessTime(DateUtil.toLocalDateTime(entry.getLastAccessTime()));
        node.setDir(entry.isDirectory());
        if (!entry.isDirectory()) {
            node.setSize(entry.getSize());
            node.setCrc(entry.getCrc());
            createNodeFileAndHash(node, bytes);
        }
        return node;
    }
}
