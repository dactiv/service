package com.github.dactiv.service.resource.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.AlgorithmProperties;
import com.github.dactiv.framework.crypto.RsaProperties;
import com.github.dactiv.service.commons.service.SystemConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.service.resource")
public class ApplicationConfig {

    /**
     * 展示轮播图数量
     */
    private Integer carouselCount = 5;

    /**
     * 展示热搜关键字的值大于多少的内容
     */
    private Integer showKeyValueIncrementMinValue = 500;

    private CacheProperties accessCryptoCache = CacheProperties.of(
            "dactiv:service:resource:access:crypto:all"
    );

    /**
     * 存储在 redis 私有 token 缓存配置
     */
    private CacheProperties privateKeyCache = CacheProperties.of(
            "dactiv:service:resource:access:crypto:token:private:",
            new TimeProperties(30, TimeUnit.SECONDS)
    );

    /**
     * 存储在 redis 访问 token 缓存配置
     */
    private CacheProperties accessTokenKeyCache = CacheProperties.of(
            "dactiv:service:resource:access:crypto:token:",
            new TimeProperties(1800, TimeUnit.SECONDS)
    );

    /**
     * 商户缓存
     */
    private CacheProperties merchantCache = CacheProperties.of(
            "dactiv:service:resource:merchant:",
            new TimeProperties(1, TimeUnit.DAYS)
    );

    /**
     * 数据字典缓存
     */
    private CacheProperties dictionaryCache = CacheProperties.of("dactiv:service:resource:data-dictionary:");

    /**
     * 加解密算法配置
     */
    private AlgorithmProperties algorithm;

    /**
     * rsa 配置
     */
    private RsaProperties rsa;

    private int merchantAesKeySize = 256;

    private String dictionarySeparator = ".";

    /**
     * 忽略环境变量的开头值
     */
    private List<String> ignoreEnvironmentStartWith = Collections.singletonList("spring");

    /**
     * 忽略的枚举服务集合
     */
    private List<String> ignoreEnumerateService = Collections.singletonList(SystemConstants.SYS_GATEWAY_NAME);

    /**
     * 伪装访问加解密的成功信息
     */
    private String camouflageAccessCryptoName = "success access crypto";
}
