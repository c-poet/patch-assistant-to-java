package cn.cpoet.patch.assistant.fernflower;

import java.util.Map;

/**
 * 单个类反编译
 *
 * @author CPoet
 */
public class SingleClassDecompiler extends AbsClassDecompiler<SingleClassDecompiler> {

    private String packageName;
    private String classSource;

    public SingleClassDecompiler(Map<String, Object> customProperties) {
        super(customProperties);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassSource() {
        return classSource;
    }

    @Override
    public void saveClassFile(String s, String s1, String s2, String s3, int[] ints) {
        this.packageName = s1;
        this.classSource = s3;
    }
}
