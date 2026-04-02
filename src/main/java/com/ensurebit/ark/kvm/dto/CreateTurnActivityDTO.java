package com.ensurebit.ark.kvm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreateTurnActivityDTO {

    @Schema(description = "Bytes received")
    private Integer bytesReceived;

    @Schema(description = "Bytes sent")
    private Integer bytesSent;
}
