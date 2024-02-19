package com.github.dactiv.service.dmp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableScheduling
@EnableWebSecurity
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableMethodSecurity(securedEnabled = true)
@EnableFeignClients("com.github.dactiv.service.commons.service.feign")
@EnableRedisHttpSession(redisNamespace = "dactiv:service:spring:session")
@SpringBootApplication(scanBasePackages = {"com.github.dactiv.service.dmp", "com.github.dactiv.service.commons.service"}, exclude = DataSourceAutoConfiguration.class)
public class DataMiddlePlatformMain {

    public static void main(String[] args) {
        SpringApplication.run(DataMiddlePlatformMain.class, args);
    }
}
