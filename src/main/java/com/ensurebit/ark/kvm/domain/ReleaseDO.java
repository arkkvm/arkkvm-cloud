package com.ensurebit.ark.kvm.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("releases")
public class ReleaseDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "Version")
    private String version;

    @Schema(description = "Download URL")
    private String url;

    @Schema(description = "File hash")
    private String hash;

    @Schema(description = "Type: app or system")
    private String type;

    @Schema(type = "string", description = "Summary")
    private String summary;

    @Schema(type = "int", description = "Rollout percentage")
    private Integer gray;

    @Schema(type = "boolean", description = "Preview")
    private Boolean isPreview;

    @Schema(description = "Sort order")
    private Long sortOrder;
}
