package cn.cpoet.patch.assistant.view.node_mapped;

import cn.cpoet.patch.assistant.control.tree.node.TreeNode;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.util.I18nUtil;
import cn.cpoet.patch.assistant.util.UIUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;

/**
 * 查看最新绑定的信息
 *
 * @author CPoet
 */
public class NodeMappedView extends AbsNodeMappedView {

    public NodeMappedView(TreeNode appRootNode, TreeNode patchRootNode) {
        super(appRootNode, patchRootNode, true);
    }

    @Override
    protected TextArea createTextArea() {
        TextArea textArea = super.createTextArea();
        textArea.setEditable(false);
        return textArea;
    }

    private DialogPane createDialogPane(Stage stage) {
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(build());
        Configuration configuration = Configuration.getInstance();
        dialogPane.setPrefSize(configuration.getNodeMappedWidth(), configuration.getNodeMappedHeight());
        dialogPane.widthProperty().addListener((observableValue, oldVal, newVal) -> configuration.setNodeMappedWidth(newVal.doubleValue()));
        dialogPane.heightProperty().addListener((observableValue, oldVal, newVal) -> configuration.setNodeMappedHeight(newVal.doubleValue()));
        ButtonType saveAsFileBT = new ButtonType(I18nUtil.t("app.view.node-mapped.save-as-file"), ButtonBar.ButtonData.APPLY);
        dialogPane.getButtonTypes().add(saveAsFileBT);
        ButtonType copyInfoBT = new ButtonType(I18nUtil.t("app.view.node-mapped.copy-info"), ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(copyInfoBT);
        dialogPane.getButtonTypes().add(ButtonType.CANCEL);
        dialogPane.lookupButton(saveAsFileBT).addEventFilter(ActionEvent.ACTION, e -> {
            handleSaveAsFile(stage);
            e.consume();
        });
        Button copyInfoBtn = (Button) dialogPane.lookupButton(copyInfoBT);
        copyInfoBtn.addEventFilter(ActionEvent.ACTION, e -> {
            handleCopyInfo();
            copyInfoBtn.setDisable(true);
            copyInfoBtn.setText(I18nUtil.t("app.view.node-mapped.copy-info-ok"));
            UIUtil.timeout(3, TimeUnit.SECONDS, () -> UIUtil.runUI(() -> {
                copyInfoBtn.setDisable(false);
                copyInfoBtn.setText(I18nUtil.t("app.view.node-mapped.copy-info"));
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
        dialog.setTitle(I18nUtil.t("app.view.node-mapped.title"));
        dialog.setDialogPane(createDialogPane(stage));
        dialog.showAndWait();
    }
}
