package com.github.dactiv.service.gateway.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.crypto.AlgorithmProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 访问加解密配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties(prefix = "dactiv.service.gateway")
public class ApplicationConfig {

    /**
     * request 中的客户端密文参数名
     */
    private String cipherTextParamName = "cipherText";

    /**
     * 访问 token header 名称
     */
    private String accessTokenHeaders = "X-ACCESS-TOKEN";

    /**
     * 参数与值的分隔符
     */
    private String paramNameValueDelimiter = "=";

    /**
     * 存储在 redis 访问 token 缓存配置
     */
    private CacheProperties accessTokenKeyCache = CacheProperties.of("dactiv:service:access:crypto:token:");

    /**
     * 存储所有访问加解密的缓存配置
     */
    private CacheProperties accessCryptoCache = CacheProperties.of("dactiv:service:access:crypto:all");

    /**
     * 当错误获取不到响应的 ReasonPhrase 时，抛出异常的默认信息
     */
    private String defaultReasonPhrase = ErrorCodeException.DEFAULT_ERROR_MESSAGE;

    /**
     * 加解密算法配置
     */
    private AlgorithmProperties algorithm;
}
