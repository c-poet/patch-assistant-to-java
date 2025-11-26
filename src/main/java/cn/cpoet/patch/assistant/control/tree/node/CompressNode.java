package cn.cpoet.patch.assistant.control.tree.node;

import java.time.LocalDateTime;

/**
 * @author CPoet
 */
public class CompressNode extends FileNode {

    /**
     * 是否目录
     */
    private boolean isDir;

    /**
     * 压缩文件注释信息
     */
    private String comment;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;

    /**
     * 修改时间
     */
    private LocalDateTime modifyTime;

    /**
     * 校验值
     */
    private long crc = -1;

    /**
     * 扩展信息
     */
    private byte[] extra;

    @Override
    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    @Override
    public LocalDateTime getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }

    public long getCrc() {
        return crc;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    public byte[] getExtra() {
        return extra;
    }

    public void setExtra(byte[] extra) {
        this.extra = extra;
    }
}
