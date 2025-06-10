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
    private final IntegerProperty addTotal = new SimpleIntegerProperty(0);

    /**
     * 更新统计
     */
    private final IntegerProperty modTotal = new SimpleIntegerProperty(0);

    /**
     * 删除统计
     */
    private final IntegerProperty delTotal = new SimpleIntegerProperty(0);

    /**
     * 标记删除统计
     */
    private final IntegerProperty markDelTotal = new SimpleIntegerProperty(0);

    public int getAddTotal() {
        return addTotal.get();
    }

    public IntegerProperty addTotalProperty() {
        return addTotal;
    }

    public void setAddTotal(int addTotal) {
        this.addTotal.set(addTotal);
    }

    public int getModTotal() {
        return modTotal.get();
    }

    public IntegerProperty modTotalProperty() {
        return modTotal;
    }

    public void setModTotal(int modTotal) {
        this.modTotal.set(modTotal);
    }

    public int getDelTotal() {
        return delTotal.get();
    }

    public IntegerProperty delTotalProperty() {
        return delTotal;
    }

    public void setDelTotal(int delTotal) {
        this.delTotal.set(delTotal);
    }

    public int getMarkDelTotal() {
        return markDelTotal.get();
    }

    public IntegerProperty markDelTotalProperty() {
        return markDelTotal;
    }

    public void setMarkDelTotal(int markDelTotal) {
        this.markDelTotal.set(markDelTotal);
    }

    public void rest() {
        setAddTotal(0);
        setModTotal(0);
        setDelTotal(0);
        setMarkDelTotal(0);
    }

    /**
     * 判断是否存在绑定的节点（非删除的）
     *
     * @return 是否存在绑定的节点
     */
    public boolean isMappedAddOrModNode() {
        return getAddTotal() + getModTotal() > 0;
    }

    /**
     * 判断是否存在变化的节点
     *
     * @return 是否存在变化的节点
     */
    public boolean isChangeNode() {
        return getAddTotal() > 0 || getModTotal() > 0 || getDelTotal() > 0 || getMarkDelTotal() > 0;
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
