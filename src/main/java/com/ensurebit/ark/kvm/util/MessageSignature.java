package com.ensurebit.ark.kvm.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

@Component
@Slf4j
public class MessageSignature {

    private final PrivateKey privateKey;


    public MessageSignature(ResourceLoader resourceLoader,
                            @Value("${sign.keystore.path}") String keyStore,
                            @Value("${sign.keystore.password}") String keyStorePassword) throws Exception {
        log.info("keyStore: {}", keyStore);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        Resource resource = resourceLoader.getResource(keyStore);

        try (InputStream is = resource.getInputStream()) {
            ks.load(is, keyStorePassword.toCharArray());
        }

        String alias = ks.aliases().nextElement();
        this.privateKey = (PrivateKey) ks.getKey(alias, keyStorePassword.toCharArray());
    }

    public String sign(String message) throws Exception {
        byte[] data = message.getBytes();
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(data);
        byte[] sign = signer.sign();
        return Base64.getEncoder().encodeToString(sign);
    }
}
