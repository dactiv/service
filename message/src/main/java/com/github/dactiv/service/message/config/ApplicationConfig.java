package com.github.dactiv.service.message.config;

import com.github.dactiv.service.commons.service.domain.meta.CloudSecurityMeta;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties("dactiv.service.message")
public class ApplicationConfig {

    /**
     * 腾讯云配置
     */
    private CloudSecurityMeta tencentCloud = new CloudSecurityMeta();

}
