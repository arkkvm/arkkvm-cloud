package com.ensurebit.ark.kvm.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface WebSocketMessageType {
    @JsonIgnore
    String getMessageType();
}
