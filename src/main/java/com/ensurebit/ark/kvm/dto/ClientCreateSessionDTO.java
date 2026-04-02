package com.ensurebit.ark.kvm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ClientCreateSessionDTO {

    @Schema(description = "Device ID")
    @NotBlank
    private String id;

    @Schema(description = "description")
    @NotBlank
    private String sd;
}
