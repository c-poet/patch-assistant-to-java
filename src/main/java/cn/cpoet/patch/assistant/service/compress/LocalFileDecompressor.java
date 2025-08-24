package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.core.Configuration;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author CPoet
 */
public class LocalFileDecompressor extends FileDecompressor {

    public static final LocalFileDecompressor INSTANCE = new LocalFileDecompressor();

    @Override
    public void decompress(InputStream in, UnCallback callback) {
        String unrarPath = checkAndGetUnRarPath();
        File file = save2LocalFile(in);
        File dir = createTempDir();
        try {
            decompress(unrarPath, dir, file);
            deepGetFile(dir, callback);
        } catch (Exception e) {
            FileTempUtil.deleteAllTempFile(dir);
            throw e;
        } finally {
            FileTempUtil.deleteTempFile(file);
        }
    }

    private void decompress(String unrarPath, File dir, File file) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{unrarPath, "x", file.getPath(), dir.getPath()});
            waitDecompress(process);
        } catch (Exception e) {
            throw new AppException("Failed to decompress using the local tool", e);
        }
    }

    private void waitDecompress(Process process) throws IOException {
        try (InputStream in = process.getInputStream()) {
            if (in.readAllBytes().length == 0) {
                // TODO BY CPoet 空语句体处理方案
                System.out.println();
            }
        }
    }

    private void deepGetFile(File file, UnCallback callback) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File childFile : files) {
                doDeepGetFile(childFile, "", callback);
            }
        }
    }

    private void doDeepGetFile(File file, String path, UnCallback callback) {
        LocalFileInfo fileInfo = new LocalFileInfo();
        fileInfo.setFile(file);
        fileInfo.setPath(FileNameUtil.joinPath(path, file.getName()));
        callback.invoke(fileInfo, null);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    doDeepGetFile(childFile, fileInfo.getPath(), callback);
                }
            }
        }
    }

    private File createTempDir() {
        File tempDir = AppContext.getInstance().getTempDir();
        return FileUtil.mkdir(tempDir, UUIDUtil.random32());
    }

    private File save2LocalFile(InputStream in) {
        File tempDir = AppContext.getInstance().getTempDir();
        try {
            return FileTempUtil.writeFile2TempDir(tempDir, UUIDUtil.random32() + FileExtConst.DOT_RAR, in.readAllBytes());
        } catch (IOException e) {
            throw new AppException("", e);
        }
    }

    private String checkAndGetUnRarPath() {
        String unrarPath = Configuration.getInstance().getPatch().getUnrarPath();
        if (!StringUtil.isBlank(unrarPath)) {
            File file = new File(unrarPath);
            if (file.isDirectory()) {
                file = new File(file, getUnrarProgramName());
            }
            if (file.exists()) {
                return file.getPath();
            }
        }
        throw new AppException("There is no tool available locally that can operate on RAR files");
    }

    private String getUnrarProgramName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "unrar.exe";
        }
        return "unrar";
    }
}
