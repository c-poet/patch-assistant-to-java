package cn.cpoet.patch.assistant.view.home;

import cn.cpoet.patch.assistant.control.form.TextDynamicWidthField;
import cn.cpoet.patch.assistant.control.tree.CustomTreeView;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.util.CollectionUtil;
import cn.cpoet.patch.assistant.util.StringUtil;
import cn.cpoet.patch.assistant.util.UIUtil;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author CPoet
 */
public class FastSearchControl {

    private int firstSelectIndex;
    private final TextField textField;
    private final AtomicInteger version;
    private final AtomicInteger curIndex;
    private final CustomTreeView<?> treeView;
    private final AtomicReference<List<TreeNode>> searchResults;

    public FastSearchControl(CustomTreeView<?> treeView) {
        this.treeView = treeView;
        this.textField = new TextDynamicWidthField();
        this.textField.setVisible(false);
        this.version = new AtomicInteger(0);
        this.curIndex = new AtomicInteger(0);
        this.searchResults = new AtomicReference<>(null);
        bindEvent();
    }

    private void bindEvent() {
        treeView.setOnKeyTyped(textField::fireEvent);
        textField.textProperty().addListener((observableValue, oldVal, newVal) -> {
            if (StringUtil.isEmpty(newVal)) {
                textField.setVisible(false);
            } else {
                if (!textField.isVisible()) {
                    firstSelectIndex = treeView.getSelectionModel().getSelectedIndex();
                    textField.setVisible(true);
                }
                search(newVal);
            }
        });
    }

    public void search(String keyword) {
        int curVersion = version.incrementAndGet();
        search(keyword, curVersion, firstSelectIndex);
    }

    private void search(String keyword, int curVersion, int firstSelectIndex) {
        UIUtil.runNotUI(() -> {
            int targetIndex = 0;
            int distance = Integer.MAX_VALUE;
            List<TreeNode> searchResults = new ArrayList<>();
            Queue<TreeNode> pendingQueue = new LinkedList<>();
            pendingQueue.add(treeView.getTreeInfo().getRootNode());
            while (!pendingQueue.isEmpty()) {
                TreeNode node = pendingQueue.poll();
                if (node.getName().contains(keyword)) {
                    searchResults.add(node);
                    int curDistance = firstSelectIndex - treeView.getRow(node.getTreeItem());
                    if (curDistance == 0) {
                        distance = curDistance;
                        targetIndex = searchResults.size() - 1;
                    } else if (curDistance < 0 && distance > 0) {
                        distance = curDistance;
                        targetIndex = searchResults.size() - 1;
                    } else if (!(curDistance > 0 && distance < 0) && Math.abs(curDistance) < Math.abs(distance)) {
                        distance = curDistance;
                        targetIndex = searchResults.size() - 1;
                    }
                }
                if (node.getTreeItem().isExpanded()) {
                    pendingQueue.addAll(node.getChildren());
                }
            }
            updateSearchResult(searchResults, targetIndex, curVersion);
        });
    }

    private void updateSearchResult(List<TreeNode> searchResults, int targetIndex, int curVersion) {
        if (curVersion != version.get()) {
            return;
        }
        synchronized (this) {
            if (curVersion == version.get()) {
                this.curIndex.set(targetIndex);
                this.searchResults.set(searchResults);
                selectNext();
            }
        }
    }

    public boolean selectPre() {
        if (CollectionUtil.isEmpty(searchResults.get())) {
            return false;
        }
        synchronized (this) {
            List<TreeNode> searchResults = this.searchResults.get();
            if (CollectionUtil.isEmpty(searchResults)) {
                return false;
            }
            TreeNode node = searchResults.get(curIndex.getAndUpdate(val -> {
                if (--val < 0) {
                    return searchResults.size() - 1;
                }
                return val;
            }));
            selectItem(node);
        }
        return true;
    }

    public boolean selectNext() {
        if (CollectionUtil.isEmpty(searchResults.get())) {
            return false;
        }
        synchronized (this) {
            List<TreeNode> searchResults = this.searchResults.get();
            if (CollectionUtil.isEmpty(searchResults)) {
                return false;
            }
            TreeNode node = searchResults.get(curIndex.getAndUpdate(val -> {
                if (++val >= searchResults.size()) {
                    return 0;
                }
                return val;
            }));
            selectItem(node);
        }
        return true;
    }

    private void selectItem(TreeNode node) {
        UIUtil.runUI(() -> {
            TreeItem<TreeNode> treeItem = node.getTreeItem();
            int index = treeView.getRow(treeItem);
            treeView.scrollTo(index);
            treeView.getSelectionModel().clearSelection();
            treeView.getSelectionModel().select(index);
        });
    }

    public boolean isSearch() {
        return textField.isVisible();
    }

    public void handleCancel() {
        textField.setText(null);
        synchronized (this) {
            this.searchResults.set(null);
            this.curIndex.set(0);
            this.version.set(0);
        }
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
