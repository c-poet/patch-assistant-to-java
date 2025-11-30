package cn.cpoet.patch.assistant.view.readme;

import cn.cpoet.patch.assistant.control.tree.PatchRootInfo;
import cn.cpoet.patch.assistant.control.tree.PatchTreeView;
import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.service.PatchPackService;
import cn.cpoet.patch.assistant.util.AlterUtil;
import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.util.UIUtil;
import cn.cpoet.patch.assistant.view.node_mapped.AbsNodeMappedView;
import cn.cpoet.patch.assistant.view.progress.ProgressView;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 查看最新绑定的信息
 *
 * @author CPoet
 */
public class ReadmeView extends AbsNodeMappedView {

    private final TreeNode readmeNode;
    private final PatchTreeView patchTree;
    private final Consumer<Boolean> callback;

    public ReadmeView(TreeNode readmeNode, PatchTreeView patchTree, TreeNode appRootNode, TreeNode patchRootNode, Consumer<Boolean> callback) {
        super(appRootNode, patchRootNode, readmeNode == null);
        this.readmeNode = readmeNode;
        this.patchTree = patchTree;
        this.callback = callback;
    }

    protected Node build() {
        Node node = super.build();
        if (readmeNode != null) {
            PatchRootInfo rootInfo = patchTree.getTreeInfo().getRootInfoByNode(patchRootNode);
            if (rootInfo != null && rootInfo.getPatchSign() != null) {
                codeEditor.getCodeArea().replaceText(rootInfo.getPatchSign().getReadme());
            }
        }
        return node;
    }

    protected void handleGenMappedInfo() {
        buildMappedInfo();
    }

    @Override
    protected ContextMenu createContentMenu() {
        ContextMenu contentMenu = super.createContentMenu();
        contentMenu.getItems().add(getIncludeDelMenuItem());
        MenuItem genMappedInfoMenuItem = new MenuItem(I18nUtil.t("app.view.readme.gen-mapped-info"));
        genMappedInfoMenuItem.setOnAction(e -> handleGenMappedInfo());
        contentMenu.getItems().add(genMappedInfoMenuItem);
        return contentMenu;
    }

    @Override
    protected String getSaveFileName() {
        return readmeNode != null ? readmeNode.getName() : super.getSaveFileName();
    }

    @Override
    protected void updateText(String text) {
        UIUtil.runUI(() -> codeEditor.getCodeArea().replaceSelection(text));
    }

    private boolean handleUpdateReadme(Stage stage) {
        if (readmeNode != null) {
            ButtonType bt = AlterUtil.confirm(stage, I18nUtil.t("app.view.readme.update-tip"), ButtonType.YES, ButtonType.NO);
            if (!ButtonType.YES.equals(bt)) {
                return false;
            }
        }
        String text = codeEditor.getCodeArea().getText();
        new ProgressView(I18nUtil.t(readmeNode == null ? "app.view.readme.create-task-name" : "app.view.readme.update-task-name"))
                .showDialog(stage, progressContext -> PatchPackService.INSTANCE.updatePatchReadme(progressContext, patchTree, patchRootNode, text, callback));
        return true;
    }

    private DialogPane createDialogPane(Stage stage) {
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(build());
        Configuration configuration = Configuration.getInstance();
        dialogPane.setPrefSize(configuration.getReadmeWidth(), configuration.getReadmeHeight());
        dialogPane.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setReadmeWidth(newVal.doubleValue()));
        dialogPane.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setReadmeHeight(newVal.doubleValue()));
        ButtonType updateReadmeBT = new ButtonType(I18nUtil.t(readmeNode == null ? "app.view.readme.create" : "app.view.readme.update"), ButtonBar.ButtonData.YES);
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
