package cn.cpoet.patch.assistant.control.tree.node;

import java.time.LocalDateTime;

/**
 * @author CPoet
 */
public class VirtualNode extends TreeNode {

    private boolean isDir;
    private LocalDateTime modifyTime;

    @Override
    public boolean isDir() {
        return isDir;
    }

    @Override
    public LocalDateTime getModifyTime() {
        return modifyTime;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }
}
