package com.ensurebit.ark.kvm.handle;

import cn.hutool.core.collection.ConcurrentHashSet;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class DeviceSessionManager {


    private final Map<String, WebSocketSession> deviceSessions = new ConcurrentHashMap<>();


    private final Map<String, DeviceMessageCallback> deviceMessageCallbacks = new ConcurrentHashMap<>();


    private final Set<String> inFlight = new ConcurrentHashSet<>();


    public boolean isInFlight(String deviceId) {
        return inFlight.contains(deviceId);
    }

    public boolean hasDevice(String deviceId) {
        return deviceSessions.containsKey(deviceId);
    }

    public WebSocketSession getDeviceSession(String deviceId) {
        return deviceSessions.get(deviceId);
    }

    public void addDeviceSession(String deviceId, WebSocketSession session) {
        deviceSessions.put(deviceId, session);
    }

    public void removeDeviceSession(String deviceId) throws IOException {
        deviceSessions.remove(deviceId).close();
    }

    public boolean send(String deviceId, String message) {

        return false;
    }

    public DeviceMessageCallback getDeviceMessageCallback(String deviceId) {
        return deviceMessageCallbacks.get(deviceId);
    }

    public void setDeviceMessageCallback(String deviceId, DeviceMessageCallback callback) {
        deviceMessageCallbacks.put(deviceId, callback);
    }

    public void removeDeviceMessageCallback(String deviceId) {
        deviceMessageCallbacks.remove(deviceId);
    }
}
