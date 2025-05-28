package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.exception.AppException;
import cn.cpoet.patch.assistant.util.FileUtil;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.InputStream;

/**
 * 应用
 *
 * @author CPoet
 */
public class AppContext extends ServiceFactory {

    private static AppContext INSTANCE;

    private Configuration configuration;

    private AppContext() {
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public AppContext reload() {
        try (InputStream in = FileUtil.getFileAsStream(AppConst.CONFIG_FILE_NAME)) {
            if (in == null) {
                configuration = new Configuration();
                syncConf2File();
            } else {
                configuration = buildXmlMapper().readValue(in, Configuration.class);
            }
        } catch (Exception e) {
            throw new AppException("配置文件读取失败", e);
        }
        return this;
    }

    public void syncConf2File() {
        try {
            byte[] bytes = buildXmlMapper().writeValueAsBytes(configuration);
            FileUtil.writeFile(AppConst.CONFIG_FILE_NAME, bytes);
        } catch (Exception e) {
            throw new AppException("配置文件写入失败", e);
        }
    }

    private XmlMapper buildXmlMapper() {
        return XmlMapper.builder()
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .defaultUseWrapper(true)
                .build();
    }

    public void destroy() {
        super.destroy();
        this.syncConf2File();
    }

    public static AppContext getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppContext().reload();
        }
        return INSTANCE;
    }
}
