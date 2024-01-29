package com.github.dactiv.service.authentication.security.handler;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.spring.security.authentication.config.SpringSecurityProperties;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationFailureResponse;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.service.authentication.config.ApplicationConfig;
import com.github.dactiv.service.authentication.enumerate.AuthenticationTypeEnum;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.SystemErrorCodeConstants;
import com.github.dactiv.service.commons.service.domain.meta.IdValueMeta;
import com.github.dactiv.service.commons.service.feign.ResourceServiceFeignClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * json 形式的认证失败具柄实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class CaptchaAuthenticationFailureResponse implements JsonAuthenticationFailureResponse {

    public static final String ALLOWABLE_FAILURE_NUMBER_NAME = "failureNumber";

    @Getter
    private final ApplicationConfig applicationConfig;

    @Getter
    private final SpringSecurityProperties springSecurityProperties;

    @Getter
    private final ResourceServiceFeignClient resourceServiceFeignClient;

    private final RedissonClient redissonClient;

    @Override
    public void setting(RestResult<Map<String, Object>> result, HttpServletRequest request, AuthenticationException e) {

        if (e instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException exception = Casts.cast(e);
            result.setExecuteCode(exception.getError().getErrorCode());
            return;
        }

        Map<String, Object> data = result.getData();

        if (AuthenticationCredentialsNotFoundException.class.isAssignableFrom(e.getClass())) {
            result.setExecuteCode(SystemErrorCodeConstants.LOGIN_EXECUTE_CODE);
            return;
        }

        IdValueMeta<String, Map<String, Object>> meta = getAllowableFailureMeta(request);
        // 获取错误次数
        Integer number = Casts.cast(meta.getValue().getOrDefault(ALLOWABLE_FAILURE_NUMBER_NAME, 0));

        String type = request.getParameter(ApplicationConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);

        AuthenticationTypeEnum loginType = NameEnumUtils.parse(type, AuthenticationTypeEnum.class);

        if (AuthenticationTypeEnum.USERNAME.equals(loginType)) {
            data.put(ALLOWABLE_FAILURE_NUMBER_NAME, ++number);
        }

        if (number < applicationConfig.getAllowableFailureNumber()) {
            meta.setValue(data);
            saveAllowableFailureMeta(meta);
            return;
        }

        // 获取设备唯一识别
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        Map<String, Object> buildToken = resourceServiceFeignClient.createCaptchaToken(
                applicationConfig.getFormLoginFailureCaptchaType(),
                identified,
                new LinkedHashMap<>()
        );

        data.put(SystemConstants.CAPTCHA_TOKEN_NAME, buildToken);

        result.setExecuteCode(SystemErrorCodeConstants.CAPTCHA_EXECUTE_CODE);

        meta.setValue(data);
        saveAllowableFailureMeta(meta);

    }

    private void saveAllowableFailureMeta(IdValueMeta<String, Map<String, Object>> meta) {
        CacheProperties cache = applicationConfig.getAllowableFailureNumberCache();
        String key = cache.getName(meta.getId());
        RBucket<IdValueMeta<String, Map<String, Object>>> bucket = redissonClient.getBucket(key);
        TimeProperties expiresTime = cache.getExpiresTime();

        if (Objects.isNull(expiresTime)) {
            bucket.setAsync(meta);
        } else {
            bucket.setAsync(meta, expiresTime.getValue(), expiresTime.getUnit());
        }
    }

    /**
     * 是否需要验证码认证
     *
     * @param request 请求信息
     * @return true 是，否则 false
     */
    public boolean isCaptchaAuthentication(HttpServletRequest request) {
        String type = request.getParameter(ApplicationConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);
        AuthenticationTypeEnum loginType = NameEnumUtils.parse(type, AuthenticationTypeEnum.class, true);
        if (!AuthenticationTypeEnum.USERNAME.equals(loginType)) {
            return false;
        }

        IdValueMeta<String, Map<String, Object>> meta = getAllowableFailureMeta(request);
        Integer number = Casts.cast(meta.getValue().getOrDefault(CaptchaAuthenticationFailureResponse.ALLOWABLE_FAILURE_NUMBER_NAME, 0));

        return number >= applicationConfig.getAllowableFailureNumber();
    }

    /**
     * 删除允许认证失败次数
     *
     * @param request 请求信息
     */
    public void deleteAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = applicationConfig.getAllowableFailureNumberCache().getName(identified);

        redissonClient.getBucket(key).deleteAsync();
    }

    public IdValueMeta<String, Map<String, Object>> getAllowableFailureMeta(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);
        String key = applicationConfig.getAllowableFailureNumberCache().getName(identified);
        RBucket<IdValueMeta<String, Map<String, Object>>> bucket = redissonClient.getBucket(key);

        IdValueMeta<String, Map<String, Object>> result = new IdValueMeta<>();
        result.setId(identified);
        result.setValue(new LinkedHashMap<>());
        if (bucket.isExists()) {
            result = bucket.get();
        }

        return result;
    }

}
