package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.control.form.TextDynamicWidthField;
import cn.cpoet.patch.assistant.control.tree.CustomTreeView;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author CPoet
 */
public class FastSearchControl {

    private int curResultIndex;
    private int firstSelectIndex;
    private final TextField textField;
    private List<TreeNode> searchResults;
    private final CustomTreeView<?> treeView;

    public FastSearchControl(CustomTreeView<?> treeView) {
        this.treeView = treeView;
        this.textField = new TextDynamicWidthField();
        this.textField.setVisible(false);
        bindEvent();
    }

    private void bindEvent() {
        treeView.setOnKeyTyped(textField::fireEvent);
        textField.textProperty().addListener((observableValue, oldVal, newVal) -> {
            if (StringUtil.isEmpty(newVal)) {
                textField.setVisible(false);
                clearSearch();
            } else {
                if (!textField.isVisible()) {
                    firstSelectIndex = treeView.getSelectionModel().getSelectedIndex();
                    textField.setVisible(true);
                }
                search(newVal);
            }
        });
    }

    private void search(String keyword) {
        int targetIndex = 0;
        int distance = 0;
        List<TreeNode> searchResults = new ArrayList<>();
        Queue<TreeNode> pendingQueue = new LinkedList<>();
        pendingQueue.add(treeView.getTreeInfo().getRootNode());
        while (!pendingQueue.isEmpty()) {
            TreeNode node = pendingQueue.poll();
            if (node.getName().contains(keyword)) {
                searchResults.add(node);
                int row = treeView.getRow(node.getTreeItem());
                if (distance == 0 || Math.abs(firstSelectIndex - row) < distance) {
                    distance = firstSelectIndex - row;
                    targetIndex = searchResults.size() - 1;
                }
            }
            if (node.getTreeItem().isExpanded()) {
                pendingQueue.addAll(node.getChildren());
            }
        }
        clearSearch();
        this.curResultIndex = targetIndex;
        this.searchResults = searchResults;
        selectNext();
    }

    private void clearSearch() {
        this.curResultIndex = 0;
        this.searchResults = null;
    }

    public boolean selectPre() {
        if (CollectionUtil.isEmpty(searchResults)) {
            return false;
        }
        if (curResultIndex < 0) {
            curResultIndex = searchResults.size() - 1;
        }
        TreeNode node = searchResults.get(curResultIndex--);
        selectItem(node);
        return true;
    }

    public boolean selectNext() {
        if (CollectionUtil.isEmpty(searchResults)) {
            return false;
        }
        if (curResultIndex >= searchResults.size()) {
            curResultIndex = 0;
        }
        TreeNode node = searchResults.get(curResultIndex++);
        selectItem(node);
        return true;
    }

    private void selectItem(TreeNode node) {
        TreeItem<TreeNode> treeItem = node.getTreeItem();
        int index = treeView.getRow(treeItem);
        treeView.scrollTo(index);
        treeView.getSelectionModel().clearSelection();
        treeView.getSelectionModel().select(index);
    }

    public boolean isSearch() {
        return textField.isVisible();
    }

    public void handleCancel() {
        textField.setText(null);
    }

    public void handleBackspace() {
        textField.deletePreviousChar();
    }

    public void onKeyReleased(KeyEvent event) {
        if (isSearch()) {
            if (KeyCode.BACK_SPACE.equals(event.getCode())) {
                handleBackspace();
                event.consume();
            } else if (KeyCode.ESCAPE.equals(event.getCode())) {
                handleCancel();
                event.consume();
            } else if (event.isControlDown() && KeyCode.K.equals(event.getCode()) && selectPre()) {
                event.consume();
            } else if (event.isControlDown() && KeyCode.J.equals(event.getCode()) && selectNext()) {
                event.consume();
            }
        }
    }


    public void fillNode(StackPane stackPane) {
        StackPane.setAlignment(textField, Pos.TOP_LEFT);
        stackPane.getChildren().add(textField);
    }
}
