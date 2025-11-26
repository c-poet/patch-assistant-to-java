package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.constant.ParamNameConst;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.StringUtil;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 启动信息
 * <p>不考虑多线程修改问题</p>
 *
 * @author CPoet
 */
public class StartUpInfo {

    private volatile static StartUpInfo sui;

    /**
     * 应用文件
     */
    private File appFile;

    /**
     * 标记是否从参数加载
     */
    private boolean isArgAppFile;

    /**
     * 补丁文件
     */
    private File patchFile;

    /**
     * 标记是否从参数参加
     */
    private boolean isArgPatchFile;

    public File getAppFile() {
        return appFile;
    }

    public boolean isArgAppFile() {
        return isArgAppFile;
    }

    public File getPatchFile() {
        return patchFile;
    }

    public boolean isArgPatchFile() {
        return isArgPatchFile;
    }

    public static void consume(Consumer<StartUpInfo> consumer) {
        StartUpInfo startUpInfo = sui;
        if (startUpInfo == null) {
            throw new AppException("The app startup info is not initialized");
        }
        consumer.accept(startUpInfo);
    }

    public static <T> T consume(Function<StartUpInfo, T> consumer) {
        StartUpInfo startUpInfo = sui;
        if (startUpInfo == null) {
            throw new AppException("The app startup info is not initialized");
        }
        return consumer.apply(startUpInfo);
    }

    public static <T> T run(Supplier<T> supplier) {
        initStartUpInfo();
        try {
            return supplier.get();
        } finally {
            removeStartUpInfo();
        }
    }

    private static void initStartUpInfo() {
        if (StartUpInfo.sui != null) {
            return;
        }
        StartUpInfo startUpInfo = new StartUpInfo();
        startUpInfo.init();
        StartUpInfo.sui = startUpInfo;
    }

    private static void removeStartUpInfo() {
        StartUpInfo.sui = null;
    }

    private void init() {
        initAppPackFile();
        initPatchFile();
    }

    private void initAppPackFile() {
        String targetPath = AppContext.getInstance().getArg(ParamNameConst.TARGET);
        if (!StringUtil.isBlank(targetPath) && targetPath.endsWith(FileExtConst.DOT_JAR)) {
            appFile = FileUtil.getExistsFile(targetPath);
        }
        if (appFile != null) {
            isArgAppFile = true;
            return;
        }
        String lastAppPackPath = Configuration.getInstance().getLastAppPackPath();
        if (!StringUtil.isBlank(lastAppPackPath)) {
            appFile = FileUtil.getExistsFile(lastAppPackPath);
        }
    }

    private void initPatchFile() {
        String patchPath = AppContext.getInstance().getArg(ParamNameConst.PATCH);
        if (!StringUtil.isBlank(patchPath)) {
            patchFile = FileUtil.getExistsDirOrFile(patchPath);
        }
        if (patchFile != null) {
            isArgPatchFile = true;
            return;
        }
        String targetPath = AppContext.getInstance().getArg(ParamNameConst.TARGET);
        if (!StringUtil.isBlank(targetPath) && !targetPath.endsWith(FileExtConst.DOT_JAR)) {
            patchFile = FileUtil.getExistsDirOrFile(targetPath);
        }
        if (patchFile != null) {
            isArgPatchFile = true;
            return;
        }
        // 判断是否需要加载最后操作的补丁信息
        if (Boolean.TRUE.equals(Configuration.getInstance().getPatch().getLoadLastPatch())) {
            String lastPatchPackPath = Configuration.getInstance().getLastPatchPackPath();
            if (!StringUtil.isBlank(lastPatchPackPath)) {
                patchFile = FileUtil.getExistsDirOrFile(lastPatchPackPath);
            }
        }
    }
}
