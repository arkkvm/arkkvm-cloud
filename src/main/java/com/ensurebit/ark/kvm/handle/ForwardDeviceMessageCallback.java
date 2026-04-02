package com.ensurebit.ark.kvm.handle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
public class ForwardDeviceMessageCallback implements DeviceMessageCallback {

    private final ObjectMapper objectMapper;

    private final WebSocketSession session;

    public ForwardDeviceMessageCallback(WebSocketSession session, ObjectMapper objectMapper) {
        this.session = session;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(String deviceId, TextMessage message) throws IOException {
        log.debug("[Device] onMessage: {}", message.getPayload());
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());
        if (!jsonNode.has("type")) {
            return;
        }
        String type = jsonNode.get("type").asText();

        switch (type) {
            case "answer":
                log.debug("[Device] Sending answer to client");
                session.sendMessage(message);
                break;
            case "new-ice-candidate":
                log.debug("[Device] Sending ice-candidate to client");
                session.sendMessage(message);
                break;
            default:
                log.warn("message type not support {}", type);
                break;
        }
    }
}
