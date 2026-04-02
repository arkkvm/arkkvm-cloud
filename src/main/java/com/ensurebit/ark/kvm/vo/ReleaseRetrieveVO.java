package com.ensurebit.ark.kvm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseRetrieveVO {
    @Schema(description = "App version")
    private String appVersion;

    @Schema(description = "App download URL")
    private String appUrl;

    @Schema(description = "App file hash signature")
    private String appHashSignature;

    @Schema(description = "System version")
    private String systemVersion;

    @Schema(description = "System download URL")
    private String systemUrl;

    @Schema(description = "System file hash signature")
    private String systemHashSignature;
}
