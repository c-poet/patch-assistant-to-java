package cn.cpoet.patch.assistant.view.readme;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.control.tree.PatchTreeView;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.service.compress.UnCallback;
import cn.cpoet.patch.assistant.util.*;
import cn.cpoet.patch.assistant.view.progress.ProgressView;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 查看最新绑定的信息
 *
 * @author CPoet
 */
public class ReadmeView {

    private TextArea textArea;
    private final TreeNode readmeNode;
    private final PatchTreeView patchTree;
    private final Consumer<Boolean> callback;

    public ReadmeView(TreeNode readmeNode, PatchTreeView patchTree, Consumer<Boolean> callback) {
        this.readmeNode = readmeNode;
        this.patchTree = patchTree;
        this.callback = callback;
    }

    private Node build() {
        textArea = new TextArea();
        textArea.setPadding(Insets.EMPTY);
        textArea.setText(patchTree.getTreeInfo().getReadmeText());
        return textArea;
    }

    private void handleCopyInfo() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(textArea.getText());
        systemClipboard.setContent(content);
    }

    private void handleSaveAsFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(readmeNode.getName());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("README FILE", "*" + FileExtConst.DOT_TXT));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            FileUtil.writeFile(file, textArea.getText().getBytes());
        }
    }

    private boolean handleUpdateReadme(Stage stage) {
        ButtonType bt = AlterUtil.confirm(stage, I18nUtil.t("app.view.readme.update-tip"), ButtonType.YES, ButtonType.NO);
        if (!ButtonType.YES.equals(bt)) {
            return false;
        }
        String text = textArea.getText();
        TreeNode rootNode = TreeNodeUtil.getUnderRootNode(readmeNode);
        new ProgressView(I18nUtil.t("app.view.readme.update-task-name")).showDialog(stage, progressContext
                -> PatchPackService.INSTANCE.updatePatchReadme(progressContext, patchTree, rootNode, text, callback));
        return true;
    }

    private DialogPane createDialogPane(Stage stage) {
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(build());
        Configuration configuration = Configuration.getInstance();
        dialogPane.setPrefSize(configuration.getReadmeWidth(), configuration.getReadmeHeight());
        dialogPane.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setReadmeWidth(newVal.doubleValue()));
        dialogPane.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setReadmeHeight(newVal.doubleValue()));
        ButtonType updateReadmeBT = new ButtonType(I18nUtil.t("app.view.readme.update"), ButtonBar.ButtonData.YES);
        dialogPane.getButtonTypes().add(updateReadmeBT);
        ButtonType saveAsFileBT = new ButtonType(I18nUtil.t("app.view.readme.save-as-file"), ButtonBar.ButtonData.APPLY);
        dialogPane.getButtonTypes().add(saveAsFileBT);
        ButtonType copyInfoBT = new ButtonType(I18nUtil.t("app.view.readme.copy-info"), ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(copyInfoBT);
        dialogPane.getButtonTypes().add(ButtonType.CANCEL);
        Button updateReadmeBtn = (Button) dialogPane.lookupButton(updateReadmeBT);
        updateReadmeBtn.addEventFilter(ActionEvent.ACTION, e -> {
            if (!handleUpdateReadme(stage)) {
                e.consume();
            }
        });
        dialogPane.lookupButton(saveAsFileBT).addEventFilter(ActionEvent.ACTION, e -> {
            handleSaveAsFile(stage);
            e.consume();
        });
        Button copyInfoBtn = (Button) dialogPane.lookupButton(copyInfoBT);
        copyInfoBtn.addEventFilter(ActionEvent.ACTION, e -> {
            handleCopyInfo();
            copyInfoBtn.setDisable(true);
            copyInfoBtn.setText(I18nUtil.t("app.view.readme.copy-info-ok"));
            UIUtil.timeout(3, TimeUnit.SECONDS, () -> UIUtil.runUI(() -> {
                copyInfoBtn.setDisable(false);
                copyInfoBtn.setText(I18nUtil.t("app.view.readme.copy-info"));
            }));
            e.consume();
        });
        return dialogPane;
    }

    public void showDialog(Stage stage) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.NONE);
        dialog.setResizable(true);
        dialog.setTitle(I18nUtil.t("app.view.readme.title"));
        dialog.setDialogPane(createDialogPane(stage));
        dialog.showAndWait();
    }
}
