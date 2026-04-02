package com.ensurebit.ark.kvm.vo;

import lombok.Data;

@Data
public class NewIceCandidateVO implements WebSocketMessageType {
    @Override
    public String getMessageType() {
        return "";
    }
}
