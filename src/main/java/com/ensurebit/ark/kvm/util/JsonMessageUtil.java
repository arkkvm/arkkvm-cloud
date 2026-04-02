package com.ensurebit.ark.kvm.util;

import com.ensurebit.ark.kvm.vo.WebSocketMessageType;
import com.ensurebit.ark.kvm.vo.WsMessageVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.web.socket.TextMessage;

public final class JsonMessageUtil {


    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static TextMessage of(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            return new TextMessage(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize object to JSON", e);
        }
    }

    public static TextMessage of(WebSocketMessageType payload) {
        return of(new WsMessageVO<>(payload.getMessageType(), payload));
    }


    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON to " + clazz.getSimpleName(), e);
        }
    }


    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON to " + typeReference.getType(), e);
        }
    }

}
