package cn.cpoet.patch.assistant.fernflower;

import cn.cpoet.patch.assistant.constant.FileExtConst;
import cn.cpoet.patch.assistant.util.FileTempUtil;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;

import java.io.File;
import java.util.Map;

/**
 * 单个类反编译
 *
 * @author CPoet
 */
public class SingleClassDecompiler extends AbsResultSaver implements IBytecodeProvider {

    private final Fernflower engine;
    private byte[] classBytes;
    private String packageName;
    private String classSource;

    public SingleClassDecompiler(Map<String, Object> customProperties) {
        this.engine = new Fernflower(this, this, customProperties, new FernflowerLogger());
    }

    public String decompile(byte[] bytes) {
        this.classBytes = bytes;
        File classFile = FileTempUtil.createTempFile(SingleClassDecompiler.class.getName(), FileExtConst.DOT_CLASS);
        try {
            engine.addSource(classFile);
            engine.decompileContext();
        } finally {
            FileTempUtil.deleteTempFile(classFile);
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
        return classBytes;
    }

    @Override
    public void saveClassFile(String s, String s1, String s2, String s3, int[] ints) {
        this.packageName = s1;
        this.classSource = s3;
    }
}
