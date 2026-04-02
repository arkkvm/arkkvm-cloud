package com.ensurebit.ark.kvm.handle;

import lombok.Getter;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableResponseCallback implements DeviceMessageCallback {

    @Getter
    private final CompletableFuture<TextMessage> future = new CompletableFuture<>();


    @Override
    public void onMessage(String deviceId, TextMessage message) throws IOException {
        future.complete(message);
    }

    public void setTimeout(long timeout) {
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Device response timeout"));
            }
        }, timeout, TimeUnit.MILLISECONDS);
    }
}
