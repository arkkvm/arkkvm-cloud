package com.ensurebit.ark.kvm.handle;

import cn.dev33.satoken.stp.StpUtil;
import com.ensurebit.ark.kvm.domain.UserDO;
import com.ensurebit.ark.kvm.mapper.UserMapper;
import com.ensurebit.ark.kvm.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.util.NumberUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Map;

@Slf4j
public class ClientSocketHandlerInterceptor implements HandshakeInterceptor {
    @Resource
    private DeviceSessionManager deviceSessionManager;

    @Resource
    private UserMapper userMapper;

    @Resource
    private DeviceService deviceService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        URI uri = request.getURI();

        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
        String deviceId = queryParams.getFirst("id");
        if (deviceId == null) {
            log.debug("device id is null");
            return false;
        }
        String token = queryParams.getFirst("token");

        if (!deviceSessionManager.hasDevice(deviceId)) {
            log.debug("device {} not on", deviceId);
            return false;
        }

        if (deviceSessionManager.isInFlight(deviceId)) {
            log.debug("device {} an inflight client connection.", deviceId);
            return false;
        }

        Object idByToken = StpUtil.getLoginIdByToken(token);
        if (idByToken == null) {
            log.warn("client token not match {}-{}", deviceId, token);
            return false;
        }
        Long id = NumberUtils.parseNumber(idByToken.toString(), Long.class);

        if (!deviceService.hasDevice(deviceId, id)) {
            log.debug("device not found");
            return false;
        }
        UserDO userDO = userMapper.selectById(id);
        if (userDO == null) {
            log.debug("user not found");
            return false;
        }
        log.info("user id {},connect to device {}", id, deviceId);
        attributes.put("oidcToken", userDO.getOidcToken());
        attributes.put("deviceId", deviceId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
