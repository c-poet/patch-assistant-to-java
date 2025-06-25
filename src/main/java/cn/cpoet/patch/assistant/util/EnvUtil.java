package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.constant.AppConst;

/**
 * 环境工具
 *
 * @author CPoet
 */
public abstract class EnvUtil {

    private EnvUtil() {
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getUserName() {
        String userName = System.getProperty(AppConst.APP_NAME + ".user.name");
        if (!StringUtil.isBlank(userName)) {
            return userName;
        }
        return System.getProperty("user.name");
    }
}
