package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.InputStream;

/**
 * XML工具
 *
 * @author CPoet
 */
public abstract class XMLUtil {

    private static XmlMapper xmlMapper;

    private XMLUtil() {
    }

    public static XmlMapper getXmlMapper() {
        if (xmlMapper == null) {
            xmlMapper = buildXmlMapper();
        }
        return xmlMapper;
    }

    public static <T> T read(InputStream in, Class<T> clazz) {
        try {
            return getXmlMapper().readValue(in, clazz);
        } catch (Exception e) {
            throw new AppException("反序列化失败", e);
        }
    }

    public static <T> T read(byte[] bytes, TypeReference<T> typeReference) {
        try {
            return getXmlMapper().readValue(bytes, typeReference);
        } catch (Exception e) {
            throw new AppException("反序列化失败", e);
        }
    }

    public static byte[] writeAsBytes(Object obj) {
        try {
            return getXmlMapper().writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new AppException("序列化失败", e);
        }
    }

    private static XmlMapper buildXmlMapper() {
        return XmlMapper.builder()
                .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .defaultUseWrapper(true)
                .build();
    }
}
