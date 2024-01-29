package com.github.dactiv.service.authentication;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableScheduling
@EnableDiscoveryClient
@EnableConfigurationProperties
@EnableFeignClients("com.github.dactiv.service.commons.service.feign")
@EnableRedisHttpSession(redisNamespace = "dactiv:service:spring:session")
@SpringBootApplication(scanBasePackages = {"com.github.dactiv.service.authentication", "com.github.dactiv.service.commons.service"})
public class AuthenticationMain {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationMain.class, args);
    }
}
