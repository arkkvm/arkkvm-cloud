package com.ensurebit.ark.kvm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import sun.net.www.http.HttpClient;


import java.net.InetSocketAddress;
import java.net.Proxy;


@Configuration
public class RestTemplateConfig {

    @Value("${proxy.host:}")
    private String proxyHost;

    @Value("${proxy.port:0}")
    private int proxyPort;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        if (StringUtils.hasText(proxyHost) && proxyPort > 0) {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
            restTemplate.setRequestFactory(requestFactory);
        }
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }
}
