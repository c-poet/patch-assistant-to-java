package cn.cpoet.patch.assistant.view.tree;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * 统计信息
 *
 * @author CPoet
 */
public class TotalInfo {
    /**
     * 新增统计
     */
    private int addTotal;

    /**
     * 更新统计
     */
    private int modTotal;

    /**
     * 删除统计
     */
    private int delTotal;

    /**
     * 标记删除统计
     */
    private int markDelTotal;

    /**
     * 变更事件统计
     */
    private final IntegerProperty changeTotal = new SimpleIntegerProperty(0);

    public int getAddTotal() {
        return addTotal;
    }

    public void setAddTotal(int addTotal) {
        this.addTotal = addTotal;
        this.emitChangeEvent();
    }

    public int getModTotal() {
        return modTotal;
    }

    public void setModTotal(int modTotal) {
        this.modTotal = modTotal;
        this.emitChangeEvent();
    }

    public int getDelTotal() {
        return delTotal;
    }

    public void setDelTotal(int delTotal) {
        this.delTotal = delTotal;
        this.emitChangeEvent();
    }

    public int getMarkDelTotal() {
        return markDelTotal;
    }

    public void setMarkDelTotal(int markDelTotal) {
        this.markDelTotal = markDelTotal;
        this.emitChangeEvent();
    }


    public void rest() {
        this.addTotal = 0;
        this.modTotal = 0;
        this.delTotal = 0;
        this.markDelTotal = 0;
        this.emitChangeEvent();
    }

    /**
     * 判断是否存在绑定的节点（非删除的）
     *
     * @return 是否存在绑定的节点
     */
    public boolean isMappedAddOrModNode() {
        return addTotal + modTotal > 0;
    }

    /**
     * 判断是否存在变化的节点
     *
     * @return 是否存在变化的节点
     */
    public boolean isChangeNode() {
        return addTotal > 0 || modTotal > 0 || delTotal > 0 || markDelTotal > 0;
    }

    public int getChangeTotal() {
        return changeTotal.get();
    }

    public IntegerProperty changeTotalProperty() {
        return changeTotal;
    }

    private void emitChangeEvent() {
        changeTotal.set(getChangeTotal() + 1);
    }

    public void incrTotal(TreeNodeStatus status) {
        switch (status) {
            case ADD:
                setAddTotal(getAddTotal() + 1);
                break;
            case MOD:
                setModTotal(getModTotal() + 1);
                break;
            case DEL:
                setDelTotal(getDelTotal() + 1);
                break;
            case MARK_DEL:
                setMarkDelTotal(getMarkDelTotal() + 1);
                break;
            case NONE:
            default:
        }
    }

    public void decrTotal(TreeNodeStatus status) {
        switch (status) {
            case ADD:
                setAddTotal(getAddTotal() - 1);
                break;
            case MOD:
                setModTotal(getModTotal() - 1);
                break;
            case DEL:
                setDelTotal(getDelTotal() - 1);
                break;
            case MARK_DEL:
                setMarkDelTotal(getMarkDelTotal() - 1);
                break;
            case NONE:
            default:
        }
    }
}
