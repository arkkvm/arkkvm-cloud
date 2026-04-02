package com.ensurebit.ark.kvm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UpdateDeviceDTO {

    @Schema(description = "Device name")
    @NotBlank(message = "Device name cannot be blank")
    @Size(min = 3, max = 20, message = "Invalid device name length")
    private String name;
}
