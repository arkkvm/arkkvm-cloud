package com.ensurebit.ark.kvm.service;

import com.ensurebit.ark.kvm.vo.ReleaseRetrieveVO;
import org.springframework.http.ResponseEntity;

import java.net.URISyntaxException;

public interface ReleaseService {
    ReleaseRetrieveVO retrieve(String appVersion, String systemVersion) throws Exception;

    ResponseEntity<String> retrieveLatestApp() throws URISyntaxException;

    ResponseEntity<String> retrieveLatestSystemRecovery() throws URISyntaxException;
}
