package cn.cpoet.patch.assistant.core;

import java.util.Properties;

/**
 * 应用配置
 *
 * @author CPoet
 */
public class AppConfig {

    private final Properties properties;

    /**
     * 配置实例
     */
    private static final AppConfig INSTANCE = new AppConfig().reload();

    private AppConfig() {
        properties = new Properties();
    }

    /**
     * 重新加载配置
     *
     * @return 应用配置
     */
    public AppConfig reload() {
        return this;
    }
}
