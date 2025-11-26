package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.util.FileTempUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.StringUtil;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
        Set<String> allPidSet = ProcessHandle.allProcesses()
                .map(ProcessHandle::pid)
                .map(String::valueOf)
                .collect(Collectors.toSet());
        File[] files = file.listFiles(childFile -> {
            if (!childFile.getName().startsWith(AppConst.APP_NAME)) {
                return false;
            }
            String lockPid = FileUtil.readDirLockInfo(childFile);
            return lockPid == null || !allPidSet.contains(lockPid);
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
