package com.ensurebit.ark.kvm.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.ensurebit.ark.kvm.domain.DeviceDO;
import com.ensurebit.ark.kvm.mapper.DeviceMapper;
import com.ensurebit.ark.kvm.service.DeviceService;
import com.ensurebit.ark.kvm.service.UserService;
import com.ensurebit.ark.kvm.util.SecureCookieUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
@Tag(name = "OAuth2")
public class OAuthController {

    @Resource
    private RestTemplate restTemplate;

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Value("${oauth2.google.client-secret}")
    private String clientSecret;

    @Value("${oauth2.google.redirect-uri}")
    private String redirectUriTemplate;

    @Value("${oauth2.google.auth-uri}")
    private String authUri;

    @Value("${oauth2.google.token-uri}")
    private String tokenUri;

    @Value("${oauth2.google.scope}")
    private String scope;

    @Resource
    private UserService userService;

    @Resource
    private SecureCookieUtil secureCookieUtil;

    @Resource
    private DeviceService deviceService;

    @Resource
    private DeviceMapper deviceMapper;

    @PostMapping("/oidc/google")
    @Operation(summary = "Google authorization page")
    public void authorize(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam String returnTo,
                          @RequestParam(required = false) String deviceId) throws Exception {
        secureCookieUtil.setCookie(response, "returnTo", returnTo);

        if (deviceId != null) {
            secureCookieUtil.setCookie(response, "deviceId", deviceId);
            log.debug("deviceId: {}", deviceId);
        }

        String url = UriComponentsBuilder
                .fromUriString(authUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUriTemplate.replace("{baseUrl}", getBaseUrl(request)))
                .queryParam("response_type", "code")
                .queryParam("scope", scope)
                .queryParam("prompt", "consent")
                .build()
                .toUriString();

        response.sendRedirect(url);
    }

    @GetMapping("/oauth2/code/google")
    public ResponseEntity<String> callback(@RequestParam("code") String code,
                                           HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {
        String returnTo = secureCookieUtil.getCookieValue(request, "returnTo");
        if (returnTo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No returnTo cookie found");
        }

        String deviceId = secureCookieUtil.getCookieValue(request, "deviceId");
        secureCookieUtil.clearCookie(response, "deviceId");
        secureCookieUtil.clearCookie(response, "returnTo");

        ResponseEntity<OAuthExchangeTokenResponse> tokenResponse = exchangeToken(code, getBaseUrl(request));
        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Token endpoint returned " + tokenResponse.getStatusCode());
        }

        OAuthExchangeTokenResponse body = tokenResponse.getBody();
        if (body == null || body.getAccessToken() == null) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Token endpoint returned invalid response");
        }

        String idToken = body.getIdToken();
        log.debug("idToken: {}", idToken);
        Long userId = userService.upsertUserByGoogleAuth(idToken);
        if (deviceId == null) {
            StpUtil.login(userId);
        } else {
            if (!deviceService.bindDevice(deviceId, userId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Device already bound");
            }
            StpUtil.login(userId, deviceId);
        }

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(returnTo)
                .queryParam("token", tokenInfo.getTokenValue())
                .queryParam("clientId", clientId)
                .queryParam("oidcGoogle", idToken);

        if (deviceId != null) {
            builder.queryParam("deviceId", deviceId);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(builder.build().toUri())
                .build();
    }

    @GetMapping("/oidc/quick")
    @Operation(summary = "Quick login")
    public ResponseEntity<String> quickLogin(@RequestParam String deviceId) {
        DeviceDO deviceDO = deviceMapper.selectById(deviceId);
        if (deviceDO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
        }
        StpUtil.login(deviceDO.getUserId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return ResponseEntity.ok(tokenInfo.getTokenValue());
    }

    private ResponseEntity<OAuthExchangeTokenResponse> exchangeToken(String code, String baseUrl) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUriTemplate.replace("{baseUrl}", baseUrl));
        form.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        return restTemplate.postForEntity(tokenUri, entity, OAuthExchangeTokenResponse.class);
    }

    @Data
    public static class OAuthExchangeTokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expires_in")
        private int expiresIn;

        private String scope;

        @JsonProperty("id_token")
        private String idToken;
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        boolean isDefaultPort = ("http".equals(scheme) && serverPort == 80)
                || ("https".equals(scheme) && serverPort == 443);
        return scheme + "://" + serverName + (isDefaultPort ? "" : ":" + serverPort) + contextPath;
    }
}
