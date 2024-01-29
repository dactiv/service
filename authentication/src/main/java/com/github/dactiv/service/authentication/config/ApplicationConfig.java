package com.github.dactiv.service.authentication.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.SystemConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties("dactiv.service.authentication")
public class ApplicationConfig {

    public static final String DEFAULT_LOGIN_TYPE_PARAM_NAME = "loginType";

    public static final String DEFAULT_WAKE_UP_TOKEN_NAME = "wakeUpToken";

    public static final String DEFAULT_LOGOUT_URL = "/logout";

    /**
     * 管理员组 id
     */
    private Integer adminGroupId = 1;

    /**
     * 允许认证错误次数，当达到峰值时，出现验证码
     */
    private Integer allowableFailureNumber = 3;

    /**
     * 表单登录错误使用的验证码类型
     */
    private String formLoginFailureCaptchaType = "tianai";

    /**
     * app 登录错误使用的验证码类型
     */
    private String appLoginCaptchaType = "tianai";

    /**
     * 超级管理登录账户
     */
    private String adminUsername = "admin";

    /**
     * 允许登录失败次数的缓存配置
     */
    private CacheProperties allowableFailureNumberCache = CacheProperties.of(
            "dactiv:service:authentication:failure:",
            TimeProperties.of(1800, TimeUnit.SECONDS)
    );

    private TimeProperties oauth2SecretKeyExpiresTime = TimeProperties.of(365, TimeUnit.DAYS);

    /**
     * 登出连接
     */
    private String logoutUrl = DEFAULT_LOGOUT_URL;

    /**
     * 忽略的插件服务集合
     */
    private List<String> ignorePluginService = Collections.singletonList(SystemConstants.SYS_GATEWAY_NAME);

    /**
     * 忽略当前用户的插件集合
     */
    private Map<String, List<String>> ignorePrincipalResource = new LinkedHashMap<>();

    private List<String> desensitizePrincipalProperties = Arrays.asList(
            SecurityUserDetailsConstants.PHONE_NUMBER_KEY,
            SecurityUserDetailsConstants.EMAIL_KEY
    );

}
