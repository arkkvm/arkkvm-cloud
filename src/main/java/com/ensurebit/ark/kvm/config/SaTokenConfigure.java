package com.ensurebit.ark.kvm.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class SaTokenConfigure implements WebMvcConfigurer {

    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
                    SaRouter.match("/**", StpUtil::checkLogin);
                }))
                .addPathPatterns("/**")
                .excludePathPatterns("/doc.html")
                .excludePathPatterns("*.html", "*.css", "*.js")
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/oidc/**")
                .excludePathPatterns("/oauth2/**")
                .excludePathPatterns("/releases/**");
    }

    @Bean
    public SaServletFilter getSaServletFilter() {

        return new SaServletFilter()
                .setBeforeAuth(obj -> {
                    SaHolder.getResponse()

                            .setHeader("Access-Control-Allow-Origin", "*")
                            .setHeader("Access-Control-Allow-Methods", "*")
                            .setHeader("Access-Control-Allow-Headers", "*")
                            .setHeader("Access-Control-Max-Age", "3600")
                    ;

                    SaRouter.match(SaHttpMethod.OPTIONS).free(r -> {

                    }).back();
                });
    }

}
