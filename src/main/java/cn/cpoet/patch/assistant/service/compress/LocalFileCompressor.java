package cn.cpoet.patch.assistant.service.compress;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.core.AppContext;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.FileNameUtil;
import cn.cpoet.patch.assistant.util.FileTempUtil;
import cn.cpoet.patch.assistant.util.FileUtil;
import cn.cpoet.patch.assistant.util.UUIDUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author CPoet
 */
public class LocalFileCompressor extends FileCompressor {

    public static final LocalFileCompressor INSTANCE = new LocalFileCompressor();

    @Override
    public void decompress(InputStream in, UnCallback callback) {
        // TODO By CPoet 调用本地失败
       /* String unrarPath = checkAndGetUnRarPath();
        File file = save2LocalFile(in);
        File dir = createTempDir();
        decompress(unrarPath, dir, file);*/
    }

    private void decompress(String unrarPath, File dir, File file) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{unrarPath, "x", file.getPath(), dir.getPath()});
        } catch (Exception e) {
            throw new AppException("Failed to decompress using the local tool", e);
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
        String path = "C:\\Program Files\\WinRAR";
        File file = new File(path, "unrar.exe");
        if (file.exists()) {
            return file.getPath();
        }
        throw new AppException("There is no tool available locally that can operate on RAR files");
    }
}
