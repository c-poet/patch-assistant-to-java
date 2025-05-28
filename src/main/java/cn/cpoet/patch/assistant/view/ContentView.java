package cn.cpoet.patch.assistant.view;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.fernflower.BytecodeProvider;
import cn.cpoet.patch.assistant.fernflower.FernflowerLogger;
import cn.cpoet.patch.assistant.fernflower.ResultSaver;
import cn.cpoet.patch.assistant.util.FileTempUtil;
import cn.cpoet.patch.assistant.view.tree.TreeKindNode;
import javafx.stage.Stage;
import org.jetbrains.java.decompiler.main.Fernflower;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 内容视图
 *
 * @author CPoet
 */
public class ContentView {

    public void showDialog(Stage stage, TreeKindNode node) {
        if (node.getName().endsWith(FileExtConst.DOT_CLASS)) {
            // 创建临时文件
            File tempFile = FileTempUtil.createTempFile(node.getName());
            try {
                BytecodeProvider bytecodeProvider = new BytecodeProvider();
                bytecodeProvider.setFile(tempFile);
                bytecodeProvider.setBytes(node.getBytes());
                Map<String, Object> a = new HashMap<>();
                Fernflower fernflower = new Fernflower(bytecodeProvider, new ResultSaver(), a, new FernflowerLogger());
                fernflower.addSource(tempFile);
                fernflower.decompileContext();
            } finally {
                FileTempUtil.deleteTempFile(tempFile);
            }
        }
    }
}
