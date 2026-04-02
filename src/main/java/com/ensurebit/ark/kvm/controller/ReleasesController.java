package com.ensurebit.ark.kvm.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.ensurebit.ark.kvm.service.ReleaseService;
import com.ensurebit.ark.kvm.vo.ReleaseRetrieveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.net.URISyntaxException;

@RestController
@Slf4j
@Tag(name = "Release management")
public class ReleasesController {

    @Resource
    private ReleaseService releaseService;

    @GetMapping("/releases")
    @Operation(summary = "Get releases")
    @SaIgnore
    public ResponseEntity<ReleaseRetrieveVO> retrieve(@RequestParam String appVersion,
                                                      @RequestParam String systemVersion) throws Exception {
        return ResponseEntity.ok(releaseService.retrieve(appVersion, systemVersion));
    }

    @GetMapping("/releases/app/latest")
    @Operation(summary = "Get latest app release")
    @SaIgnore
    public ResponseEntity<String> retrieveLatestApp() throws URISyntaxException {
        return releaseService.retrieveLatestApp();
    }

    @GetMapping("/releases/system_recovery/latest")
    @Operation(summary = "Get latest system recovery release")
    @SaIgnore
    public ResponseEntity<String> retrieveLatestSystemRecovery() throws URISyntaxException {
        return releaseService.retrieveLatestSystemRecovery();
    }
}
