package com.ensurebit.ark.kvm.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ExchangeDeviceTokenDTO {
    @NotBlank
    private String tempToken;
}
