package com.ensurebit.ark.kvm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ensurebit.ark.kvm.domain.ReleaseDO;
import com.ensurebit.ark.kvm.mapper.ReleaseMapper;
import com.ensurebit.ark.kvm.service.ReleaseService;
import com.ensurebit.ark.kvm.util.MessageSignature;
import com.ensurebit.ark.kvm.util.VersionConverter;
import com.ensurebit.ark.kvm.vo.ReleaseRetrieveVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;

@Service
@Slf4j
public class ReleaseServiceImpl implements ReleaseService {

    @Resource
    private ReleaseMapper releaseMapper;

    @Value("${release.download-cdn}")
    private String downloadCdn;

    @Resource
    private MessageSignature messageSignature;


    public ReleaseRetrieveVO retrieve(String appVersion, String systemVersion) throws Exception {

        long appVersionLong = VersionConverter.versionToLong(appVersion);
        long systemVersionLong = VersionConverter.versionToLong(systemVersion);


        ReleaseDO latestAppRelease = getLatestRelease("app");
        ReleaseDO latestSystemRelease = getLatestRelease("system");

        ReleaseRetrieveVO.ReleaseRetrieveVOBuilder builder = ReleaseRetrieveVO.builder();


        if (latestAppRelease != null) {
            if (appVersionLong >= latestAppRelease.getSortOrder()) {

            } else {
                builder.appVersion(latestAppRelease.getVersion())
                        .appUrl(downloadCdn + latestAppRelease.getUrl())
                        .appHashSignature(messageSignature.sign(latestAppRelease.getHash()));
            }
        }

        if (latestSystemRelease != null) {
            if (systemVersionLong >= latestSystemRelease.getSortOrder()) {

            } else {
                builder.systemVersion(latestSystemRelease.getVersion())
                        .systemUrl(downloadCdn + latestSystemRelease.getUrl())
                        .systemHashSignature(messageSignature.sign(latestSystemRelease.getHash()));
            }
        }

        return builder.build();
    }

    @Override
    public ResponseEntity<String> retrieveLatestApp() throws URISyntaxException {

        ReleaseDO latestAppRelease = getLatestRelease("app");
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(new URI(latestAppRelease.getUrl()))
                .build();
    }

    @Override
    public ResponseEntity<String> retrieveLatestSystemRecovery() throws URISyntaxException {

        ReleaseDO latestSystemRelease = getLatestRelease("system");
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(new URI(latestSystemRelease.getUrl()))
                .build();
    }

    private ReleaseDO getLatestRelease(String type) {

        return releaseMapper.selectOne(new LambdaQueryWrapper<ReleaseDO>()
                .eq(ReleaseDO::getType, type)
                .orderByDesc(ReleaseDO::getSortOrder)
                .last("LIMIT 1"));
    }

    private boolean isDeviceEligibleForLatestRelease(Integer rolloutPercentage, String deviceId) {
        if (rolloutPercentage == 100) return true;
        String hashedPwd = DigestUtils.md5DigestAsHex(deviceId.getBytes());
        long hashValue = Long.parseLong(hashedPwd.substring(0, 8), 16) % 100;
        return hashValue < rolloutPercentage;
    }

}
