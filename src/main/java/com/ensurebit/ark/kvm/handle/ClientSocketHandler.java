package com.ensurebit.ark.kvm.handle;

import com.ensurebit.ark.kvm.util.JsonMessageUtil;
import com.ensurebit.ark.kvm.vo.DeviceMetadataVO;
import com.ensurebit.ark.kvm.vo.DeviceOfferVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
public class ClientSocketHandler extends TextWebSocketHandler {

    @Resource
    private DeviceSessionManager deviceSessionManager;

    @Resource
    private ObjectMapper objectMapper;

    @Value("${webrtc.iceServers}")
    private List<String> iceServers;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String deviceId = (String) session.getAttributes().get("deviceId");
        WebSocketSession deviceSession = deviceSessionManager.getDeviceSession(deviceId);
        String deviceVersion = (String) deviceSession.getAttributes().get("deviceVersion");

        session.sendMessage(JsonMessageUtil.of(new DeviceMetadataVO(deviceVersion)));
        session.setTextMessageSizeLimit(81920);
        session.setBinaryMessageSizeLimit(81920);
        deviceSessionManager.setDeviceMessageCallback(deviceId, new ForwardDeviceMessageCallback(session, objectMapper));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("received client message: {}", payload);
        if (payload.equals("ping")) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        String deviceId = (String) session.getAttributes().get("deviceId");
        WebSocketSession deviceSession = deviceSessionManager.getDeviceSession(deviceId);
        if (deviceSession == null) {
            log.info("device {} session not found", deviceId);
            session.close();
            return;
        }

        JsonNode jsonNode = objectMapper.readTree(message.getPayload());
        if (!jsonNode.has("type")) {
            log.warn("message type not found");
            return;
        }

        String type = jsonNode.get("type").asText();
        switch (type) {
            case "offer":
                String sd = jsonNode.get("data").get("sd").asText();
                log.debug("receive offer send to device {}, sd {}", deviceId, sd);
                DeviceOfferVO deviceOfferVO = new DeviceOfferVO();
                deviceOfferVO.setSd(sd);
                deviceOfferVO.setIp((String) deviceSession.getAttributes().get("ip"));
                deviceOfferVO.setIceServers(iceServers);
                deviceOfferVO.setOidcGoogle((String) session.getAttributes().get("oidcToken"));
                TextMessage textMessage = JsonMessageUtil.of(deviceOfferVO);
                deviceSession.sendMessage(textMessage);
                break;
            case "new-ice-candidate":
                log.info("receive ice-candidate to device {}", deviceId);
                deviceSession.sendMessage(message);
                break;
            default:
                log.warn("message type not support");
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String deviceId = (String) session.getAttributes().get("deviceId");
        log.info("client {} connection closed: {}", deviceId, session.getId());
        deviceSessionManager.removeDeviceMessageCallback(deviceId);
    }
}
