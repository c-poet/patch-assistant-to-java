package cn.cpoet.patch.assistant.util;

import cn.cpoet.patch.assistant.exception.AppException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * JSON工具
 *
 * @author CPoet
 */
public abstract class JsonUtil {

    private static JsonMapper jsonMapper;

    private JsonUtil() {
    }

    public static JsonMapper getJsonMapper() {
        if (jsonMapper == null) {
            jsonMapper = buildJsonMapper();
        }
        return jsonMapper;
    }

    public static <T> T read(byte[] bytes, TypeReference<T> typeReference) {
        try {
            return getJsonMapper().readValue(bytes, typeReference);
        } catch (Exception e) {
            throw new AppException("反序列化失败", e);
        }
    }

    public static byte[] writeAsBytes(Object obj) {
        try {
            return getJsonMapper().writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new AppException("序列化失败", e);
        }
    }

    private static JsonMapper buildJsonMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}
