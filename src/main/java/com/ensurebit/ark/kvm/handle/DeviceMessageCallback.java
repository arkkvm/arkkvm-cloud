package com.ensurebit.ark.kvm.handle;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

public interface DeviceMessageCallback {
    void onMessage(String deviceId, TextMessage message) throws IOException;
}
