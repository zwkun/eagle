package yj.eagle.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import yj.eagle.exception.EagleException;

import java.io.IOException;

/**
 * @author zwk
 * @version 1.0
 * @date 2023/8/18 9:19
 */

public class JacksonUtil {
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }


    public static byte[] objToBytes(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new EagleException(e);
        }
    }

    public static <T> T bytesToObj(byte[] bs, Class<T> clazz) {
        try {
            return objectMapper.readValue(bs, clazz);
        } catch (IOException e) {
            throw new EagleException(e);
        }
    }
}
