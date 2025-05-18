package cn.cpoet.patch.assistant.core;

import cn.cpoet.patch.assistant.constant.AppConst;
import cn.cpoet.patch.assistant.util.FileUtil;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.File;
import java.io.InputStream;

/**
 * 应用
 *
 * @author CPoet
 */
public class Application {

    private static Application INSTANCE;

    private Configuration configuration;

    private Application() {
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Application reload() {
        try (InputStream in = FileUtil.getFileAsStream(AppConst.CONFIG_FILE_NAME)) {
            if (in == null) {
                configuration = new Configuration();
                syncConf2File();
            } else {
                configuration = buildXmlMapper().readValue(in, Configuration.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void syncConf2File() {
        try {
            buildXmlMapper().writeValue(new File(AppConst.CONFIG_FILE_NAME), configuration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private XmlMapper buildXmlMapper() {
        return XmlMapper.builder()
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .defaultUseWrapper(true)
                .build();
    }

    public static Application getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Application().reload();
        }
        return INSTANCE;
    }
}
