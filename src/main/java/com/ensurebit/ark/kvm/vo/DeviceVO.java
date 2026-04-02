package com.ensurebit.ark.kvm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class DeviceVO {

    @Schema(description = "Device ID")
    private String id;

    @Schema(description = "Device name")
    private String name;

    @Schema(description = "Device last seen time")
    private Date lastSeen;

    @Schema(description = "Device online status")
    private boolean online;

    @Schema(description = "Device cloud version")
    private String cloudVersion;
}
