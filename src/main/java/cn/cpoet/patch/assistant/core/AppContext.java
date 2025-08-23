package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.ThemeEnum;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.FileTempUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.XMLUtil;
import javafx.scene.Scene;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 应用
 *
 * @author CPoet
 */
public class AppContext {

    private static AppContext INSTANCE;

    private Scene mainScene;
    /**
     * 全局缓存目录
     */
    private File globalTempDir;
    /**
     * 启动参数，一次性解析不考虑线程安全问题
     */
    private Map<String, String> params;
    private Configuration configuration;

    private AppContext() {
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 初始化主题
     *
     * @param scene 主场景
     */
    public void initTheme(Scene scene) {
        if (mainScene == null) {
            mainScene = scene;
            updateTheme();
            scene.getStylesheets().add(FileUtil.getResourceAndExternalForm(AppConst.STYLE_FILE_NAME));
        }
    }

    /**
     * 获取当前主题
     *
     * @return 当前主题
     */
    public ThemeEnum curTheme() {
        String theme = configuration.getGenera().getTheme();
        return ThemeEnum.ofCode(theme);
    }

    /**
     * 更新主题
     */
    public void updateTheme() {
        if (mainScene != null) {
            if (ThemeEnum.DARK.equals(curTheme())) {
                mainScene.getStylesheets().add(FileUtil.getResourceAndExternalForm(AppConst.STYLE_DARK_FILE_NAME));
            } else {
                mainScene.getStylesheets().remove(FileUtil.getResourceAndExternalForm(AppConst.STYLE_DARK_FILE_NAME));
            }
        }
    }

    /**
     * 获取启动参数
     *
     * @param name 参数名称
     * @return 参数或者Null值
     */
    public String getArg(String name) {
        return getArg(name, null);
    }

    /**
     * 获取启动参数
     *
     * @param name       参数名称
     * @param defaultVal 默认值
     * @return 参数或者指定默认值
     */
    public String getArg(String name, String defaultVal) {
        return params == null ? defaultVal : params.getOrDefault(name, defaultVal);
    }

    /**
     * 解析启动参数
     *
     * @param args 启动参数
     */
    public void initArgs(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }
        Pattern pattern = Pattern.compile("^--([a-zA-Z0-9_-]+)=(.+)$");
        for (String arg : args) {
            Matcher matcher = pattern.matcher(arg);
            if (!matcher.find()) {
                continue;
            }
            if (params == null) {
                params = new HashMap<>(arg.length());
            }
            params.put(matcher.group(1), matcher.group(2));
        }
    }

    public AppContext reload() {
        try (InputStream in = FileUtil.getFileAsStream(AppConst.CONFIG_FILE_NAME)) {
            if (in == null) {
                configuration = new Configuration();
                syncConf2File();
            } else {
                configuration = XMLUtil.read(in, Configuration.class);
            }
        } catch (Exception e) {
            throw new AppException("配置文件读取失败", e);
        }
        return this;
    }

    public void syncConf2File() {
        try {
            byte[] bytes = XMLUtil.writeAsBytes(configuration);
            FileUtil.writeFile(AppConst.CONFIG_FILE_NAME, bytes);
        } catch (Exception e) {
            throw new AppException("配置文件写入失败", e);
        }
    }

    public File getTempDir() {
        if (globalTempDir != null && globalTempDir.exists()) {
            return globalTempDir;
        }
        synchronized (AppContext.class) {
            if (globalTempDir == null) {
                globalTempDir = FileTempUtil.createTempDir(AppConst.APP_NAME).toFile();
                FileUtil.writeDirLockInfo(globalTempDir);
            }
        }
        return globalTempDir;
    }

    public void destroy() {
        this.syncConf2File();
        if (globalTempDir != null && globalTempDir.exists()) {
            FileTempUtil.deleteAllTempFile(globalTempDir);
        }
    }

    public static AppContext getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppContext().reload();
        }
        return INSTANCE;
    }
}
