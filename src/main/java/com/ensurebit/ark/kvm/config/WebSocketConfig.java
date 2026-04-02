package com.ensurebit.ark.kvm.config;

import com.ensurebit.ark.kvm.handle.ClientSocketHandler;
import com.ensurebit.ark.kvm.handle.ClientSocketHandlerInterceptor;
import com.ensurebit.ark.kvm.handle.DeviceSocketHandleInterceptor;
import com.ensurebit.ark.kvm.handle.DeviceSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(deviceSocketHandler(), "/")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketHandleInterceptor());

        registry.addHandler(clientSocketHandler(), "/webrtc/signaling/client")
                .setAllowedOrigins("*")
                .addInterceptors(clientSocketHandlerInterceptor());
    }

    @Bean
    public DeviceSocketHandler deviceSocketHandler() {
        return new DeviceSocketHandler();
    }

    @Bean
    public ClientSocketHandler clientSocketHandler() {
        return new ClientSocketHandler();
    }

    @Bean
    public DeviceSocketHandleInterceptor webSocketHandleInterceptor() {
        return new DeviceSocketHandleInterceptor();
    }

    @Bean
    public ClientSocketHandlerInterceptor clientSocketHandlerInterceptor() {
        return new ClientSocketHandlerInterceptor();
    }
}
