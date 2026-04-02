package com.ensurebit.ark.kvm.vo;

import lombok.Data;

import java.util.List;

@Data
public class CloudFlareTurnGenerateVO {
    private ICEServer iceServers;

    @Data
    public static class ICEServer {
        private List<String> urls;
        private String username;
        private String credential;
    }
}
