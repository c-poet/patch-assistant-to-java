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
 * @author CPoet
 */
public abstract class AbsClassDecompiler<T extends AbsClassDecompiler<T>> extends AbsResultSaver implements IBytecodeProvider {

    protected final Fernflower engine;
    protected Map<String, byte[]> classBytesMap;

    public AbsClassDecompiler(Map<String, Object> customProperties) {
        this.engine = new Fernflower(this, this, customProperties, new FernflowerLogger());
    }

    public T decompile(byte[] bytes) {
        return decompile(bytes, Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    public T decompile(byte[] bytes, List<byte[]> innerBytes) {
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
        return (T) this;
    }

    @Override
    public byte[] getBytecode(String s, String s1) {
        return classBytesMap.get(s);
    }
}
