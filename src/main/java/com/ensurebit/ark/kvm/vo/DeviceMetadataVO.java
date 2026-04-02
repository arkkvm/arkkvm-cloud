package com.ensurebit.ark.kvm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceMetadataVO implements WebSocketMessageType {


    private String deviceVersion;

    @Override
    public String getMessageType() {
        return "device-metadata";
    }
}
