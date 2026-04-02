package com.ensurebit.ark.kvm.handle;

import com.ensurebit.ark.kvm.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;

@Slf4j
public class DeviceSocketHandler extends TextWebSocketHandler {

    @Resource
    private DeviceSessionManager deviceSessionManager;

    @Resource
    private DeviceService deviceService;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String deviceId = (String) session.getAttributes().get("deviceId");
        String deviceVersion = (String) session.getAttributes().get("deviceVersion");
        String cloudVersion = (String) session.getAttributes().get("cloudVersion");
        log.info("device {} connected, version: {} , cloudVersion: {} session_id: {}", deviceId, deviceVersion, cloudVersion, session.getId());
        session.setTextMessageSizeLimit(81920);
        session.setBinaryMessageSizeLimit(81920);
        deviceSessionManager.addDeviceSession(deviceId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String deviceId = (String) session.getAttributes().get("deviceId");
        log.debug("receive message from device {}", deviceId);

        DeviceMessageCallback messageCallback = deviceSessionManager.getDeviceMessageCallback(deviceId);
        if (messageCallback == null) {
            log.info("device {} message callback not found", deviceId);
            return;
        }
        messageCallback.onMessage(deviceId, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String deviceId = (String) session.getAttributes().get("deviceId");
        log.info("device {} connection close: {}", deviceId, session.getId());
        deviceSessionManager.removeDeviceSession(deviceId);

        deviceService.updateLastSeen(deviceId);
    }
}
