package com.ensurebit.ark.kvm.handle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ensurebit.ark.kvm.domain.DeviceDO;
import com.ensurebit.ark.kvm.mapper.DeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;


@Slf4j
public class DeviceSocketHandleInterceptor implements HandshakeInterceptor {

    @Resource
    private DeviceSessionManager deviceSessionManager;

    @Resource
    private DeviceMapper deviceMapper;


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("Authorization header is missing or invalid");
            return false;
        }
        String token = authorization.substring(7);
        DeviceDO deviceDO = deviceMapper.selectOne(new LambdaQueryWrapper<DeviceDO>().eq(DeviceDO::getSecretToken, token));
        if (deviceDO == null) {
            log.warn("device not found token {}", token);
            return false;
        }

        String deviceId = request.getHeaders().getFirst("x-device-id");
        String deviceVersion = request.getHeaders().getFirst("x-app-version");
        String cloudVersion = request.getHeaders().getFirst("x-cloud-version");
        if (!StringUtils.hasText(deviceId) || !deviceId.equals(deviceDO.getId())) {
            log.warn("device id/token not match {}-{}", deviceId, deviceDO.getId());
            return false;
        }

        if (deviceSessionManager.isInFlight(deviceId)) {
            log.warn("device {} an inflight client session", deviceId);
            return false;
        }

        if (deviceSessionManager.hasDevice(deviceId)) {

            WebSocketSession oldSession = deviceSessionManager.getDeviceSession(deviceId);
            log.warn("device {} already connected close existing session {}", deviceId, oldSession.getId());
            oldSession.close();
        }
        String realIp = getRealIp(request);
        log.info("device {},real ip {}", deviceId, realIp);

        if (!Objects.equals(deviceDO.getCloudVersion(), cloudVersion)) {
            deviceDO.setCloudVersion(cloudVersion);
            deviceMapper.updateById(deviceDO);
        }

        attributes.put("userId", deviceDO.getUserId());
        attributes.put("ip", realIp);
        attributes.put("deviceId", deviceDO.getId());
        attributes.put("deviceVersion", deviceVersion);
        attributes.put("cloudVersion", cloudVersion);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    private String getRealIp(ServerHttpRequest request){
        String forwarded = request.getHeaders().getFirst("Forwarded");
        if (forwarded != null) {

            int start = forwarded.indexOf("for=");
            if (start >= 0) {
                int end = forwarded.indexOf(";", start + 4);
                return end > start ? forwarded.substring(start + 4, end) : forwarded.substring(start + 4);
            }
        }

        return request.getRemoteAddress().getAddress().getHostAddress();
    }
}
