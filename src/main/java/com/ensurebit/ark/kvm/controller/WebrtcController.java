package com.ensurebit.ark.kvm.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ensurebit.ark.kvm.domain.TurnActivityDO;
import com.ensurebit.ark.kvm.dto.ClientCreateSessionDTO;
import com.ensurebit.ark.kvm.dto.CloudFlareTurnGenerateDTO;
import com.ensurebit.ark.kvm.dto.CreateTurnActivityDTO;
import com.ensurebit.ark.kvm.handle.CompletableResponseCallback;
import com.ensurebit.ark.kvm.handle.DeviceSessionManager;
import com.ensurebit.ark.kvm.mapper.TurnActivityMapper;
import com.ensurebit.ark.kvm.vo.BaseResult;
import com.ensurebit.ark.kvm.vo.CloudFlareTurnGenerateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@Tag(name = "WebRTC")
public class WebrtcController {

    @Resource
    private DeviceSessionManager deviceSessionManager;

    @Resource
    private TurnActivityMapper turnActivityMapper;

    @Resource
    private RestTemplate restTemplate;

    @Value("${webrtc.cloudflare.turn-key-id}")
    private String turnKeyId;

    @Value("${webrtc.cloudflare.turn-key-api-token}")
    private String turnKeyToken;

    @PostMapping("/webrtc/session")
    @Operation(summary = "Create session")
    public BaseResult<?> createSession(@RequestBody @Valid ClientCreateSessionDTO createSessionDTO) {
        if (!deviceSessionManager.hasDevice(createSessionDTO.getId())) {
            return BaseResult.fail("Device not found");
        }

        if (deviceSessionManager.isInFlight(createSessionDTO.getId())) {
            return BaseResult.fail("Device is currently processing another request");
        }

        CompletableResponseCallback callback = new CompletableResponseCallback();
        deviceSessionManager.setDeviceMessageCallback(createSessionDTO.getId(), callback);
        try {
            TextMessage textMessage = callback.getFuture().get(15000, TimeUnit.MILLISECONDS);
            return BaseResult.success(textMessage.getPayload());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            deviceSessionManager.removeDeviceMessageCallback(createSessionDTO.getId());
        }
    }

    @PostMapping("/webrtc/ice_config")
    @Schema(description = "Create ICE configuration")
    public BaseResult<CloudFlareTurnGenerateVO> createIceCredentials() {
        String url = String.format("https://rtc.live.cloudflare.com/v1/turn/keys/%s/credentials/generate", turnKeyId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(turnKeyToken);
        headers.set("User-Agent", "Application");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CloudFlareTurnGenerateDTO> entity = new HttpEntity<>(new CloudFlareTurnGenerateDTO(3600), headers);
        ResponseEntity<CloudFlareTurnGenerateVO> response = restTemplate.postForEntity(url, entity, CloudFlareTurnGenerateVO.class);
        CloudFlareTurnGenerateVO body = response.getBody();
        if (body == null) {
            return BaseResult.fail("Failed to get ICE configuration");
        }
        CloudFlareTurnGenerateVO.ICEServer iceServers = body.getIceServers();
        iceServers.setUrls(iceServers.getUrls().stream().filter(uri -> !uri.startsWith("turns")).collect(Collectors.toList()));
        return BaseResult.success(body);
    }

    @PostMapping("/webrtc/turn_activity")
    @Operation(summary = "TURN traffic statistics")
    public BaseResult<?> createTurnActivity(@RequestBody @Valid CreateTurnActivityDTO turnActivityDTO) {
        long id = StpUtil.getLoginIdAsLong();
        TurnActivityDO turnActivityDO = new TurnActivityDO();
        turnActivityDO.setBytesSent(turnActivityDTO.getBytesSent());
        turnActivityDO.setBytesReceived(turnActivityDTO.getBytesReceived());
        turnActivityDO.setCreatedAt(new Date());
        turnActivityDO.setUserId(id);
        turnActivityMapper.insert(turnActivityDO);
        return BaseResult.success();
    }
}
