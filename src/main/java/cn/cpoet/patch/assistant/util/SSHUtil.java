package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;
import com.jcraft.jsch.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * SSH连接工具
 *
 * @author CPoet
 */
public abstract class SSHUtil {

    /**
     * 创建SSH会话
     *
     * @param host     主机地址
     * @param port     端口号
     * @param username 用户名
     * @param password 密码
     * @return JSch会话对象
     */
    public static Session createSession(String host, int port, String username, String password) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            return session;
        } catch (Exception e) {
            throw new AppException("创建ssh session失败", e);
        }
    }

    /**
     * 执行SSH命令
     *
     * @param session   已连接的会话
     * @param msgOutput 消息输出
     * @param command   要执行的命令
     * @return 执行状态
     */
    public static int execCmd(Session session, OutputStream msgOutput, String command) {
        int status;
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setOutputStream(msgOutput);
            channel.setErrStream(msgOutput);
            channel.connect();
            while (!channel.isClosed()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            status = channel.getExitStatus();
        } catch (Exception e) {
            throw new AppException("执行命令失败:", e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
        return status;
    }

    /**
     * 上传单个文件
     *
     * @param session   会话信息
     * @param msgOutput 消息输出
     * @param monitor   进度监听
     * @param dir       目录
     * @param name      名称
     * @param bytes     数据
     */
    public static void uploadFile(Session session, OutputStream msgOutput, SftpProgressMonitor monitor, String dir, String name, byte[] bytes) {
        uploadFiles(session, msgOutput, monitor, dir, new String[]{name}, bytes);
    }

    /**
     * 上传多个文件到远程服务器
     *
     * @param session   已连接的会话
     * @param msgOutput 消息输出
     * @param monitor   进度监听
     * @param dir       远程文件所在目录
     * @param fileNames 文件名称
     * @param bytes     数据列表
     */
    public static void uploadFiles(Session session, OutputStream msgOutput, SftpProgressMonitor monitor, String dir, String[] fileNames, byte[]... bytes) {
        String[] paths = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; ++i) {
            paths[i] = FileNameUtil.joinPath(dir, fileNames[i]);
        }
        uploadFiles(session, msgOutput, monitor, paths, bytes);
    }

    /**
     * 上传多个文件到远程服务器
     *
     * @param session   已连接的会话
     * @param msgOutput 消息输出
     * @param monitor   进度监听
     * @param paths     远程文件路径列表
     * @param bytes     数据列表
     */
    public static void uploadFiles(Session session, OutputStream msgOutput, SftpProgressMonitor monitor, String[] paths, byte[]... bytes) {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.setInputStream(null);
            channel.setOutputStream(msgOutput);
            channel.connect();
            for (int i = 0; i < paths.length; ++i) {
                String remoteDir = FileNameUtil.getDirPath(paths[i]);
                try {
                    channel.cd(remoteDir);
                } catch (SftpException e) {
                    mkdirs(channel, remoteDir);
                }
                channel.put(new ByteArrayInputStream(bytes[i]), paths[i], monitor);
            }
        } catch (Exception e) {
            throw new AppException("文件上传失败", e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    public static void downloadFile(Session session, OutputStream msgOutput, SftpProgressMonitor monitor, String path, File file) {
        try (OutputStream out = new FileOutputStream(file)) {
            downloadFile(session, msgOutput, monitor, path, out);
        } catch (Exception e) {
            if (e instanceof AppException) {
                throw (AppException) e;
            }
            throw new AppException("下载文件失败", e);
        }
    }

    public static void downloadFile(Session session, OutputStream msgOutput, SftpProgressMonitor monitor, String path, OutputStream out) {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.setInputStream(null);
            channel.setOutputStream(msgOutput);
            channel.connect();
            channel.get(path, out, monitor);
        } catch (Exception e) {
            throw new AppException("文件下载失败", e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    /**
     * 递归创建目录（类似 mkdir -p）
     *
     * @param channel SFTP通道
     * @param path    要创建的路径
     * @throws SftpException 如果创建失败
     */
    private static void mkdirs(ChannelSftp channel, String path) throws SftpException {
        String[] folders = path.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (String folder : folders) {
            if (folder.isEmpty()) {
                continue;
            }
            currentPath.append("/").append(folder);
            try {
                channel.cd(currentPath.toString());
            } catch (SftpException e) {
                channel.mkdir(currentPath.toString());
                channel.cd(currentPath.toString());
            }
        }
    }

    /**
     * 关闭SSH会话
     *
     * @param session 要关闭的会话
     */
    public static void closeSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
