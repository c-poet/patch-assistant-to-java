package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.common.InputBufConsumer;
import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.constant.OSExplorerConst;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.model.HashInfo;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * 文件工具
 *
 * @author CPoet
 */
public abstract class FileUtil {

    public final static String[] SIZE_UNITS = {"B", "K", "M", "G"};

    private FileUtil() {
    }

    public static HashInfo getHashInfo(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return HashUtil.getHashInfo(in);
        } catch (IOException e) {
            throw new AppException("Failed to generate file hash info", e);
        }
    }

    public static void readBuf(File file, InputBufConsumer consumer) {
        try (InputStream in = new FileInputStream(file)) {
            readBuf(in, consumer);
        } catch (IOException e) {
            throw new AppException("Failed to read the file", e);
        }
    }

    /**
     * 读取输入流内容
     *
     * @param in       输入流
     * @param consumer 消费者
     * @throws IOException IO异常
     */
    public static void readBuf(InputStream in, InputBufConsumer consumer) throws IOException {
        readBuf(in, 1024, consumer);
    }

    /**
     * 读取输入流内容
     *
     * @param in       输入流
     * @param size     指定缓冲区大小
     * @param consumer 消费者
     * @throws IOException IO异常
     */
    public static void readBuf(InputStream in, int size, InputBufConsumer consumer) throws IOException {
        int len;
        byte[] buf = new byte[size];
        while ((len = in.read(buf)) != -1) {
            consumer.accept(len, buf);
        }
    }

    /**
     * 获取可读性大小值
     *
     * @param byteSize 字节大小
     * @return 大小值
     */
    public static String getSizeReadability(long byteSize) {
        int i = 0;
        float next, cur = (float) byteSize;
        while (i < SIZE_UNITS.length) {
            next = cur / 1024;
            if (next < 1) {
                break;
            }
            cur = next;
            ++i;
        }
        return String.format("%.2f", cur) + SIZE_UNITS[i < SIZE_UNITS.length ? i : i - 1];
    }

    /**
     * 读取文件并转换为{@link  String}
     *
     * @param path 文件路径
     * @return 文件内容
     */
    public static String readFileAsString(String path) {
        byte[] bytes = readFile(path);
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 读取文件
     *
     * @param file 文件
     * @return 文件内容
     */
    public static byte[] readFile(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new AppException("读取文件失败", e);
        }
    }

    /**
     * 读取文件
     *
     * @param path 文件路径
     * @return 文件内容
     */
    public static byte[] readFile(String path) {
        try (InputStream in = getFileAsStream(path)) {
            if (in == null) {
                return null;
            }
            return in.readAllBytes();
        } catch (Exception e) {
            throw new AppException("读取文件失败", e);
        }
    }

    /**
     * 获取文件输入流
     *
     * @param path 路径
     * @return 文件输入流
     */
    public static InputStream getFileAsStream(String path) {
        File file = new File(path);
        try {
            if (file.exists() && file.isFile()) {
                return new FileInputStream(file);
            }
        } catch (Exception ignored) {
        }
        if (path.startsWith(FileNameUtil.SEPARATOR)) {
            return FileUtil.class.getResourceAsStream(path);
        }
        return FileUtil.class.getResourceAsStream(FileNameUtil.SEPARATOR + path);
    }

    /**
     * 根据路径判断是否存在，并返回文件实例
     *
     * @param path 路径
     * @return 文件实例
     */
    public static File getExistsFile(String path) {
        File file = new File(path);
        return file.exists() && file.isFile() ? file : null;
    }

    /**
     * 根据路径判断是否存在，并返回文件实例
     *
     * @param path 路径
     * @return 文件实例
     */
    public static File getExistsDirOrFile(String path) {
        File file = new File(path);
        return file.exists() ? file : null;
    }

    /**
     * 写入文件
     *
     * @param file     文件
     * @param consumer 消费者
     */
    public static void writeFile(File file, Consumer<InputBufConsumer> consumer) {
        try (OutputStream out = new FileOutputStream(file)) {
            consumer.accept(((len, buf) -> out.write(buf, 0, len)));
        } catch (IOException e) {
            throw new AppException("Failed to write to the file", e);
        }
    }

    /**
     * 写入文件
     *
     * @param dir      目录
     * @param fileName 文件名
     * @param bytes    内容
     */
    public static void writeFile(String dir, String fileName, byte[] bytes) {
        String path = FileNameUtil.joinPath(dir, fileName);
        writeFile(path, bytes);
    }

    /**
     * 写入文件
     *
     * @param fileName 文件名
     * @param bytes    数据
     */
    public static void writeFile(String fileName, byte[] bytes) {
        writeFile(new File(fileName), bytes);
    }

    /**
     * 写入文件
     *
     * @param file  文件
     * @param bytes 数据
     */
    public static void writeFile(File file, byte[] bytes) {
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
            out.flush();
        } catch (Exception e) {
            throw new AppException("写入文件失败", e);
        }
    }

    /**
     * 创建目录
     *
     * @param parent 父级目录
     * @param name   名称
     */
    public static File mkdir(File parent, String name) {
        File file = new File(parent, name);
        mkdir(file);
        return file;
    }

    /**
     * 创建目录
     *
     * @param file 目录文件
     */
    public static void mkdir(File file) {
        if (file.exists()) {
            return;
        }
        if (!file.mkdirs()) {
            throw new AppException("目录创建失败");
        }
    }

    /**
     * 获取资源地址
     *
     * @param name 资源名称
     * @return 资源地址
     */
    public static String getResourceAndExternalForm(String name) {
        URL url = getResource(name);
        return url == null ? null : url.toExternalForm();
    }

    /**
     * 获取资源URL
     *
     * @param name 资源名称
     * @return 资源URL
     */
    public static URL getResource(String name) {
        return FileUtil.class.getResource(name);
    }

    /**
     * 删除文件
     *
     * @param file 文件
     * @return 是否删除成功
     */
    public static boolean deleteFile(File file) {
        return !file.exists() || file.delete();
    }

    /**
     * 移动文件
     *
     * @param source  源文件
     * @param target  目标文件
     * @param options 选项
     */
    public static void moveFile(String source, String target, CopyOption... options) {
        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);
        moveFile(sourcePath, targetPath, options);
    }

    /**
     * 移动文件
     *
     * @param source  源文件
     * @param target  目标文件
     * @param options 选项
     */
    public static void moveFile(String source, File target, CopyOption... options) {
        Path sourcePath = Paths.get(source);
        moveFile(sourcePath, target.toPath(), options);
    }

    /**
     * 移动文件
     *
     * @param source  源文件
     * @param target  目标文件
     * @param options 选项
     */
    public static void moveFile(Path source, Path target, CopyOption... options) {
        try {
            Files.move(source, target, options);
        } catch (Exception e) {
            throw new AppException("移动文件失败", e);
        }
    }

    /**
     * 在资源管理器中打开目录
     *
     * @param path 目录路径
     */
    public static void openFolder(String path) {
        if (OSUtil.isLinux()) {
            if (!OSUtil.execCommand(OSExplorerConst.LINUX_GNOME, path)
                    && !OSUtil.execCommand(OSExplorerConst.LINUX_NAUTILUS, path)) {
                OSUtil.execCommand(OSExplorerConst.LINUX_KDE, path);
            }
        } else if (OSUtil.isMacOS()) {
            OSUtil.execCommand(OSExplorerConst.MACOS, path);
        } else {
            OSUtil.execCommand(OSExplorerConst.WINDOWS, path);
        }
    }

    /**
     * 在资源管理器中选中文件
     *
     * @param filePath 文件路径
     */
    public static void selectFile(String filePath) {
        if (OSUtil.isLinux()) {
            OSUtil.execCommand(OSExplorerConst.LINUX_GNOME, filePath);
        } else if (OSUtil.isMacOS()) {
            OSUtil.execCommand(OSExplorerConst.MACOS, filePath);
        } else {
            OSUtil.execCommand(OSExplorerConst.WINDOWS, "/e,/select," + filePath);
        }
    }

    /**
     * 在目录中写入锁信息
     *
     * @param dir 目录
     */
    public static void writeDirLockInfo(File dir) {
        File file = new File(dir, AppConst.APP_LOCK_NAME);
        if (file.exists()) {
            return;
        }
        FileUtil.writeFile(file, String.valueOf(OSUtil.getPid()).getBytes());
    }

    /**
     * 读取目录中的锁信息
     *
     * @param dir 目录
     * @return 锁信息
     */
    public static String readDirLockInfo(File dir) {
        File file = new File(dir, AppConst.APP_LOCK_NAME);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        return new String(FileUtil.readFile(file));
    }

    /**
     * 拷贝源文件到指定文件
     *
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     */
    public static void copyTo(File sourceFile, File targetFile) {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(targetFile)) {
            readBuf(in, (len, buf) -> out.write(buf, 0, len));
        } catch (Exception e) {
            throw new AppException("copy file failed", e);
        }
    }
}
