package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.HashUtil;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author CPoet
 */
public class CompressNodeFileFactory extends CompressNodeFactory<LocalFileInfo> {

    public static final CompressNodeFileFactory INSTANCE = new CompressNodeFileFactory();

    @Override
    public CompressNode create(LocalFileInfo fileInfo, byte[] bytes) {
        File file = fileInfo.getFile();
        CompressNode node = new CompressNode();
        node.setName(file.getName());
        node.setPath(fileInfo.getPath());
        LocalDateTime modifyTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
        node.setCreateTime(modifyTime);
        node.setModifyTime(modifyTime);
        node.setDir(file.isDirectory());
        if (!file.isDirectory()) {
            if (bytes == null) {
                bytes = FileUtil.readFile(file);
            }
            node.setSize(bytes.length);
            node.setMd5(HashUtil.md5(bytes));
            node.setFile(file);
        }
        return node;
    }
}
