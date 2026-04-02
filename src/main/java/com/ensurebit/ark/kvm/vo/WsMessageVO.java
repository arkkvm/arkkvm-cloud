package com.ensurebit.ark.kvm.vo;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WsMessageVO<T> {
    private String type;
    private T data;
}
