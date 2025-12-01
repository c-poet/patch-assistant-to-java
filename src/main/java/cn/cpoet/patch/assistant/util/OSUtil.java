package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.exception.AppException;
import javafx.application.HostServices;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 系统工具
 *
 * @author CPoet
 */
public abstract class OSUtil {

    private static HostServices hostServices;

    private OSUtil() {
    }

    public static void initHostServices(HostServices hostServices) {
        OSUtil.hostServices = hostServices;
    }

    /**
     * 打开链接
     *
     * @param url 需要打开的链接
     */
    public static void openUrl(String url) {
        openUri(url, true);
    }

    /**
     * 打开文件
     *
     * @param path 文件路径
     */
    public static void openFile(String path) {
        openUri(path, false);
    }

    private static void openUri(String uri, boolean isUrl) {
        try {
            if (hostServices != null) {
                try {
                    UIUtil.runUI(() -> hostServices.showDocument(uri));
                } catch (Exception e) {
                    openWithDesktop(uri, isUrl);
                }
            } else {
                openWithDesktop(uri, isUrl);
            }
        } catch (Exception e) {
            throw new AppException("Open uri failed: " + uri, e);
        }
    }

    private static void openWithDesktop(String uri, boolean isUrl) {
        UIUtil.runNotUI(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (isUrl && desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI(uri));
                        return;
                    } else if (!isUrl && desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(new File(uri));
                        return;
                    }
                }
                openWithSysCommand(uri, isUrl);
            } catch (Exception e) {
                throw new AppException("Open uri failed [openUrlWithDesktop]", e);
            }
        });
    }

    private static void openWithSysCommand(String uri, boolean isUrl) throws Exception {
        Runtime rt = Runtime.getRuntime();
        if (isWindows()) {
            rt.exec("rundll32 uri.dll,FileProtocolHandler " + uri);
        } else if (isMacOS()) {
            rt.exec("open " + uri);
        } else {
            String[] browsers = !isUrl ? new String[]{"xdg-open"} :
                    new String[]{"xdg-open", "google-chrome", "firefox", "opera", "konqueror", "mozilla"};
            for (String browser : browsers) {
                if (Runtime.getRuntime().exec(new String[]{"which", browser}).waitFor() == 0) {
                    rt.exec(new String[]{browser, uri});
                    return;
                }
            }
            throw new AppException("Unable to find an available browser");
        }
    }

    /**
     * 获取当前进程的pid
     *
     * @return pid
     */
    public static long getPid() {
        return ProcessHandle.current().pid();
    }

    /**
     * 获取操作系统名称
     *
     * @return 操作系统名称
     */
    public static String getOsName() {
        return System.getProperty("os.name").toLowerCase();
    }

    /**
     * 获取当前用户家目录
     *
     * @return 家目录
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    /**
     * 判断是否是Linux操作系统
     *
     * @return 是否Linux操作系统
     */
    public static boolean isLinux() {
        return getOsName().contains("linux");
    }

    /**
     * 判断是否windows操作系统
     *
     * @return 是否windows操作系统
     */
    public static boolean isWindows() {
        return getOsName().contains("win");
    }

    /**
     * 判断是否mac操作系统
     *
     * @return 是否mac操作系统
     */
    public static boolean isMacOS() {
        return getOsName().contains("mac");
    }

    /**
     * 执行命令
     *
     * @param commandAndArgs 命令及参数
     */
    public static boolean execCommand(String... commandAndArgs) {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(commandAndArgs);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 获取应用配置路径
     *
     * @return 应用配置路径
     */
    public static Path getAppConfigPath() {
        return getAppConfigPath(AppConst.APP_NAME);
    }

    /**
     * 获取应用配置路径
     *
     * @param appName 应用名
     * @return 配置路径
     */
    public static Path getAppConfigPath(String appName) {
        Path configDir;
        if (isWindows()) {
            configDir = Paths.get(System.getenv("APPDATA"));
        } else if (isMacOS()) {
            configDir = Paths.get(getUserHome(), "Library", "Application Support");
        } else {
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            configDir = !StringUtil.isBlank(xdgConfig) ? Paths.get(xdgConfig) : Paths.get(getUserHome(), ".config");
        }
        Path appDir = configDir.resolve(appName);
        try {
            if (!Files.exists(appDir)) {
                Files.createDirectories(appDir);
            }
            return appDir;
        } catch (Exception ignored) {
        }
        // In the wrong case, try using the user home directory
        configDir = Paths.get(getUserHome(), appName);
        try {
            if (!Files.exists(appDir)) {
                Files.createDirectories(appDir);
            }
            return configDir;
        } catch (Exception e) {
            throw new AppException("Failed to get the app config directory", e);
        }
    }

    /**
     * 设置系统剪切板内容
     *
     * @param content 内容
     */
    public static void setSystemClipboard(String content) {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        setSystemClipboard(clipboardContent);
    }

    /**
     * 设置系统剪切板内容
     *
     * @param content 内容
     */
    public static void setSystemClipboard(ClipboardContent content) {
        Clipboard.getSystemClipboard().setContent(content);
    }
}
