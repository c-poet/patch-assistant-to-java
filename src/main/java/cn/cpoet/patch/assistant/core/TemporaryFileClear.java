package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.util.FileTempUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.StringUtil;

import java.io.File;

/**
 * 临时文件清理
 *
 * @author CPoet
 */
public class TemporaryFileClear {

    private void cleanOldTemporaryFile() {
        String path = System.getProperty("java.io.tmpdir");
        if (StringUtil.isBlank(path)) {
            return;
        }
        File file = FileUtil.getExistsDirOrFile(path);
        if (file == null) {
            return;
        }
        File[] files = file.listFiles((dir, name) -> name.startsWith(AppConst.APP_NAME));
        if (files != null) {
            for (File tempFile : files) {
                cleanOldTemporaryFile(tempFile);
            }
        }
    }

    private void cleanOldTemporaryFile(File file) {
        try {
            FileTempUtil.deleteAllTempFile(file);
        } catch (Exception ignored) {
        }
    }

    public static void clean() {
        new TemporaryFileClear().cleanOldTemporaryFile();
    }

    public static void asyncClean() {
        new Thread(TemporaryFileClear::clean).start();
    }
}
