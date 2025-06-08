package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;

import java.io.*;

/**
 * 本地命令工具
 *
 * @author CPoet
 */
public abstract class CommandUtil {
    private CommandUtil() {
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
    public static int exec(String command, String workDir, OutputStream msgOut, String... envs) {
        return exec(command, new File(workDir), msgOut, envs);
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
    public static int exec(String command, File workDir, OutputStream msgOut, String... envs) {
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
