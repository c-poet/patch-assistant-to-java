package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.util.FileTempUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.StringUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.CompletableFuture;

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
        long currentTime = System.currentTimeMillis();
        // 删除超时24小时的缓存（避免异常退出的导致缓存长时间累积）
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        File[] files = file.listFiles(childFile -> {
            if (!childFile.getName().startsWith(AppConst.APP_NAME)) {
                return false;
            }
            return (currentTime - file.lastModified()) > twentyFourHoursInMillis;
        });
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
        CompletableFuture.runAsync(TemporaryFileClear::clean);
    }
}
