package cn.cpoet.patch.assistant.fernflower;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.util.FileTempUtil;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单个类反编译
 *
 * @author CPoet
 */
public class SingleClassDecompiler extends AbsResultSaver implements IBytecodeProvider {

    private final Fernflower engine;
    private String packageName;
    private String classSource;
    private Map<String, byte[]> classBytesMap;

    public SingleClassDecompiler(Map<String, Object> customProperties) {
        this.engine = new Fernflower(this, this, customProperties, new FernflowerLogger());
    }

    public String decompile(byte[] bytes) {
        return decompile(bytes, Collections.emptyList());
    }

    public String decompile(byte[] bytes, List<byte[]> innerBytes) {
        classBytesMap = new HashMap<>();
        File[] classFiles = new File[innerBytes.size() + 1];
        for (int i = 0; i < classFiles.length; ++i) {
            classFiles[i] = FileTempUtil.createTempFile(SingleClassDecompiler.class.getName(), FileExtConst.DOT_CLASS);
            classBytesMap.put(classFiles[i].getPath(), i == 0 ? bytes : innerBytes.get(i - 1));
        }
        try {
            for (File classFile : classFiles) {
                engine.addSource(classFile);
            }
            engine.decompileContext();
        } finally {
            for (File classFile : classFiles) {
                FileTempUtil.deleteTempFile(classFile);
            }
        }
        return classSource;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassSource() {
        return classSource;
    }

    @Override
    public byte[] getBytecode(String s, String s1) {
        return classBytesMap.get(s);
    }

    @Override
    public void saveClassFile(String s, String s1, String s2, String s3, int[] ints) {
        this.packageName = s1;
        this.classSource = s3;
    }
}
