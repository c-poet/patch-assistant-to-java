package cn.cpoet.patch.assistant.view.tree;

import cn.cpoet.patch.assistant.util.HashUtil;

import java.time.LocalDateTime;

/**
 * 文件树形节点
 *
 * @author CPoet
 */
public abstract class TreeKindNode extends TreeNode {

    /**
     * 路径
     */
    protected String path;

    /**
     * 内容
     */
    protected byte[] bytes;

    /**
     * 内容md5值
     */
    protected String md5;

    /**
     * 内容大小
     */
    protected long size;

    /**
     * 节点状态
     */
    protected TreeNodeStatus status = TreeNodeStatus.NONE;

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
        if (md5 != null) {
            return md5;
        }
        byte[] data = getBytes();
        return data == null ? "" : (md5 = HashUtil.md5(data));
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    /**
     * 判断当前节点是否目录
     *
     * @return 是否目录
     */
    public abstract boolean isDir();

    /**
     * 获取当前节点的更新时间
     *
     * @return 当前节点的更新时间
     */
    public abstract LocalDateTime getModifyTime();

    public TreeNodeStatus getStatus() {
        return status;
    }

    public void setStatus(TreeNodeStatus status) {
        this.status = status;
    }
}
