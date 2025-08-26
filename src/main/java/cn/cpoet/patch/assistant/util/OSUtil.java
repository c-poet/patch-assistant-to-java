package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;

import java.awt.*;
import java.io.*;
import java.net.URI;

/**
 * 系统工具
 *
 * @author CPoet
 */
public abstract class OSUtil {

    private OSUtil() {
    }

    /**
     * 打开链接
     *
     * @param url 链接地址
     */
    public static void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            throw new AppException("Open url failed", e);
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
     * 执行命令并返回状态
     *
     * @param command 命令
     * @param workDir 工作目录
     * @param msgOut  消息输出
     * @param envs    环境变量
     * @return 执行状态
     */
    public static int execCommand(String command, String workDir, OutputStream msgOut, String... envs) {
        return execCommand(command, new File(workDir), msgOut, envs);
    }

    /**
     * 执行命令并返回状态
     *
     * @param command 命令
     * @param workDir 工作目录
     * @param msgOut  消息输出
     * @param envs    环境变量
     * @return 执行状态
     */
    public static int execCommand(String command, File workDir, OutputStream msgOut, String... envs) {
        try {
            Process process = Runtime.getRuntime().exec(command, envs, workDir);
            Thread outputThread = new Thread(() -> {
                try (InputStream in = process.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    readInput2Output(reader, msgOut);
                } catch (Exception e) {
                    throw new AppException("读取命令执行输出信息失败", e);
                }
                try (InputStream es = process.getErrorStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(es))) {
                    readInput2Output(reader, msgOut);
                } catch (Exception e) {
                    throw new AppException("读取命令执行错误信息失败", e);
                }
            });
            outputThread.setDaemon(true);
            outputThread.start();
            int status = process.waitFor();
            outputThread.join();
            return status;
        } catch (Exception e) {
            throw new AppException("命令执行失败", e);
        }
    }

    private static void readInput2Output(BufferedReader reader, OutputStream output) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            output.write(line.getBytes());
            output.write('\n');
            output.flush();
        }
    }
}
