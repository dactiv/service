package com.github.dactiv.service.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.service.gateway.config.ApplicationConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.stream.Collectors;

/**
 * 服务启动类
 *
 * @author maurice.chen
 */
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.github.dactiv.service.gateway")
public class GatewayMain {

    public static void main(String[] args) {
        SpringApplication.run(GatewayMain.class, args);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RestResultGatewayBlockExceptionHandler restResultGatewayBlockExceptionHandler(ObjectMapper objectMapper,
                                                                                         ApplicationConfig applicationConfig) {
        return new RestResultGatewayBlockExceptionHandler(objectMapper, applicationConfig);
    }

    @Bean
    public HttpMessageConverters httpMessageConverters(ObjectProvider<HttpMessageConverter<?>> messageConverters) {
        return new HttpMessageConverters(messageConverters.orderedStream().collect(Collectors.toList()));
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        Casts.setObjectMapper(objectMapper);
        return objectMapper;
    }
}