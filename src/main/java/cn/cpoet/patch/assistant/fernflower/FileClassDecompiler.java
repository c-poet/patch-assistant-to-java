package cn.cpoet.patch.assistant.fernflower;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.util.FileUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author CPoet
 */
public class FileClassDecompiler extends AbsClassDecompiler<FileClassDecompiler> {

    private final File dir;

    public FileClassDecompiler(File dir, Map<String, Object> customProperties) {
        super(customProperties);
        this.dir = dir;
    }

    public File getDir() {
        return dir;
    }

    @Override
    public void saveClassFile(String s, String s1, String s2, String s3, int[] ints) {
        File file = new File(dir, s1 + FileExtConst.DOT_JAVA);
        FileUtil.writeFile(file, s3.getBytes(StandardCharsets.UTF_8));
    }
}
