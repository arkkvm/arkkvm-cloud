package com.ensurebit.ark.kvm.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan("com.ensurebit.ark.kvm.mapper")
public class MybatisPlusConfig {


    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() throws  Exception{
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();


        return interceptor;
    }

}
