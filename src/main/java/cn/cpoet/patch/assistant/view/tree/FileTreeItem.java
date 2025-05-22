package cn.cpoet.patch.assistant.view.tree;

import javafx.scene.control.TreeItem;
import javafx.scene.shape.Line;

/**
 * 树形项
 *
 * @author CPoet
 */
public class FileTreeItem extends TreeItem<TreeNode> {

    private double posX;

    private double posY;

    /**
     * 关联项连接线
     */
    private Line mappedLine;

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public Line getMappedLine() {
        return mappedLine;
    }

    public void setMappedLine(Line mappedLine) {
        this.mappedLine = mappedLine;
    }
}
