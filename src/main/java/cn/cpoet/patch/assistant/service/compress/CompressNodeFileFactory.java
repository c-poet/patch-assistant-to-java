package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.control.tree.node.CompressNode;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.HashUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author CPoet
 */
public class CompressNodeFileFactory extends CompressNodeFactory<LocalFileInfo> {

    public static final CompressNodeFileFactory INSTANCE = new CompressNodeFileFactory();

    @Override
    public CompressNode create(LocalFileInfo fileInfo, InputStream in) {
        File file = fileInfo.getFile();
        CompressNode node = new CompressNode();
        node.setName(file.getName());
        node.setPath(fileInfo.getPath());
        LocalDateTime modifyTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
        node.setCreateTime(modifyTime);
        node.setModifyTime(modifyTime);
        node.setDir(file.isDirectory());
        if (!file.isDirectory()) {
            if (in == null) {
                try (InputStream tin = new FileInputStream(file)) {
                    fillNodeFileInfo(node, tin);
                } catch (IOException e) {
                    throw new AppException("Failed to read file contents", e);
                }
            } else {
                fillNodeFileInfo(node, in);
            }
            node.setFile(file);
        }
        return node;
    }

    private void fillNodeFileInfo(CompressNode node, InputStream in) {
        try {
            int[] size = new int[]{0};
            MessageDigest md5Digest = HashUtil.createMd5Digest();
            FileUtil.readBuf(in, (len, buf) -> {
                size[0] += len;
                md5Digest.update(buf, 0, len);
            });
            node.setSize(size[0]);
            node.setMd5(HashUtil.toHexStr(md5Digest));
        } catch (IOException e) {
            throw new AppException("Failed to read file contents", e);
        }
    }
}
