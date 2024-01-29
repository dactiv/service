package com.github.dactiv.service.authentication.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.github.dactiv.framework.mybatis.plus.interceptor.LastModifiedDateInnerInterceptor;
import com.github.dactiv.service.commons.service.authentication.ResourceCaptchaVerificationService;
import com.github.dactiv.service.commons.service.feign.ResourceServiceFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务配置
 *
 * @author maurice.chen
 */
@Configuration
public class ApplicationStartupAutoConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor(true));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        interceptor.addInnerInterceptor(new LastModifiedDateInnerInterceptor(true));

        return interceptor;
    }

    @Bean
    public ResourceCaptchaVerificationService captchaVerificationService(ResourceServiceFeignClient resourceServiceFeignClient) {
        return new ResourceCaptchaVerificationService(resourceServiceFeignClient);
    }

}
