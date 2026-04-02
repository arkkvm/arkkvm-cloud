package com.ensurebit.ark.kvm.vo;

import lombok.Data;

import java.util.List;

@Data
public class DeviceOfferVO implements WebSocketMessageType {

    private String sd;

    private String ip;

    private List<String> iceServers;

    private String OidcGoogle;

    @Override
    public String getMessageType() {
        return "offer";
    }

}
