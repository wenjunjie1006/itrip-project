package com.cskt.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * json工具类
 */
public class JSONUtil {
    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    /**
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    public static String objectToJsonString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 将Json字符串反序列化为Object对象
     *
     * @param jsonstring 待反序列化的Json字符串
     * @param tClass     返回的类的类型
     * @param <T>        泛型
     * @return
     * @throws IOException
     */
    public static <T> T jsonStringToObject(String jsonstring, Class<T> tClass) throws IOException {
        return objectMapper.readValue(jsonstring, tClass);
    }
}
