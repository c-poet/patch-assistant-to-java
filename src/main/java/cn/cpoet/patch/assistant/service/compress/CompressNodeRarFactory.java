package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.util.DateUtil;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import com.github.junrar.rarfile.FileHeader;

/**
 * @author CPoet
 */
public class CompressNodeRarFactory extends CompressNodeFactory<FileHeader> {

    public static final CompressNodeRarFactory INSTANCE = new CompressNodeRarFactory();


    @Override
    public CompressNode create(FileHeader header, byte[] bytes) {
        CompressNode node = new CompressNode();
        node.setName(FileNameUtil.getFileName(header.getFileName()));
        node.setText(node.getName());
        node.setPath(header.getFileName());
        node.setCreateTime(DateUtil.toLocalDateTime(header.getCreationTime()));
        node.setModifyTime(DateUtil.toLocalDateTime(header.getLastModifiedTime()));
        node.setAccessTime(DateUtil.toLocalDateTime(header.getLastAccessTime()));
        node.setDir(header.isDirectory());
        if (!header.isDirectory()) {
            node.setSize(header.getDataSize());
            node.setCrc(header.getFileCRC());
            createNodeFileAndHash(node, bytes);
        }
        return node;
    }
}
