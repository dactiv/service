package com.github.dactiv.service.resource.config.capthca;

import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import com.github.dactiv.service.commons.service.feign.ResourceServiceFeignClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 邮件验证码配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties("dactiv.service.resource.captcha.email")
public class EmailCaptchaConfig {
    /**
     * 邮件验证码的超时时间
     */
    private TimeProperties captchaExpireTime = new TimeProperties(300, TimeUnit.SECONDS);
    /**
     * 提交邮件验证码的参数名称
     */
    private String captchaParamName = ResourceServiceFeignClient.EMAIL_CAPTCHA_PARAM_NAME;

    /**
     * 邮件验证码的随机生成数量
     */
    private Integer randomNumericCount = 4;

    /**
     * 提交邮件的参数名称
     */
    private String emailParamName = "email";
    /**
     * 提交消息类型的参数名称
     */
    private String typeParamName = MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY;

    /**
     * 标题分隔符
     */
    private String titleSeparator = "】";

    /**
     * 拦截器类型
     */
    private String interceptorType = "tianai";

    /**
     * 邮件类型值
     */
    private String typeValue = "system.email.captcha.login";
}
