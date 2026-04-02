package com.ensurebit.ark.kvm.vo;

import lombok.Data;

@Data
public class RetrieveReleasesVO {

    private String appVersion;

    private String appUrl;

    private String appHash;

    private String systemVersion;

    private String systemUrl;

    private String systemHash;
}
