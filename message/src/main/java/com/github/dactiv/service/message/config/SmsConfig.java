package com.github.dactiv.service.message.config;


import com.github.dactiv.service.message.domain.meta.sms.SmsConfigPrepareMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serial;

@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "dactiv.service.message.sms")
public class SmsConfig extends SmsConfigPrepareMeta {

    @Serial
    private static final long serialVersionUID = 4064395701141428065L;

    /**
     * 渠道商
     */
    private String channel = "tencent";

    /**
     * 发送短信验证码类型
     */
    private String sendCaptchaType = "tianai";

    private Integer maxRetryCount = 3;

    /**
     * 腾讯云配置
     */
    private TencentConfig tencent = new TencentConfig();

    @Data
    public static class TencentConfig {

        private String sdkAppId;

        private String templateId;

        private String sign = "和湛科技";

        private String endpoint = "sms.tencentcloudapi.com";

        private String region = "ap-guangzhou";
    }

}
