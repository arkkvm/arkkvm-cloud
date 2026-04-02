package com.ensurebit.ark.kvm.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@TableName("device")
public class DeviceDO {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Device name")
    private String name;

    @Schema(description = "Device last seen time")
    private Date lastSeen;

    @Schema(description = "Device secret token")
    private String secretToken;

    @Schema(description = "Device cloud version")
    private String cloudVersion;
}
