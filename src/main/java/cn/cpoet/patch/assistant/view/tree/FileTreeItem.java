package cn.cpoet.patch.assistant.view.tree;

import javafx.scene.control.TreeItem;

/**
 * @author CPoet
 */
public class FileTreeItem extends TreeItem<TreeNode> {

    private double posX;

    private double posY;

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
}
