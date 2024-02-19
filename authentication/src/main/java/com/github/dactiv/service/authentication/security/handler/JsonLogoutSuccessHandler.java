package com.github.dactiv.service.authentication.security.handler;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.AnonymousUser;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.service.authentication.config.ApplicationConfig;
import com.github.dactiv.service.authentication.plugin.PluginResourceService;
import com.github.dactiv.service.authentication.service.AuthorizationService;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.SystemErrorCodeConstants;
import com.github.dactiv.service.commons.service.domain.meta.IdValueMeta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * json 形式的登出成功具柄实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    /**
     * 默认是否认证字段名
     */
    public final static String DEFAULT_AUTHENTICATED_NAME = "authenticated";

    /**
     * 当前运行的服务信息
     */
    private final static String DEFAULT_SERVICES_NAME = "services";

    private final static String DEFAULT_VERSION_NAME = "version";

    /**
     * 当前插件的服务信息
     */
    private final static String DEFAULT_PLUGIN_NAME = "pluginServices";

    @Getter
    private final ApplicationConfig applicationConfig;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    private final AuthenticationFailureResponse failureHandler;

    private final AuthorizationService authorizationService;

    private final CookieRememberService cookieRememberService;

    private final DiscoveryClient discoveryClient;

    private final PluginResourceService pluginResourceService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        HttpStatus httpStatus = SpringMvcUtils.getHttpStatus(response);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (authentication != null && SecurityUserDetails.class.isAssignableFrom(authentication.getDetails().getClass())) {
            SecurityUserDetails userDetails = Casts.cast(authentication.getDetails(), SecurityUserDetails.class);
            authorizationService.deleteSecurityUserDetailsAllCache(userDetails.toBasicUserDetails());
        }

        cookieRememberService.loginFail(request, response);

        RestResult<Map<String, Object>> result = new RestResult<>(
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                RestResult.SUCCESS_EXECUTE_CODE,
                new LinkedHashMap<>());

        response.getWriter().write(Casts.writeValueAsString(result));
    }

    /**
     * 构造未授权 reset 结果集
     *
     * @param request 请求对象
     * @return rest 结果集
     */
    public RestResult<Map<String, Object>> createUnauthorizedResult(HttpServletRequest request) {

        RestResult<Map<String, Object>> result = createRestResult(request);
        postCaptchaData(result, request);

        String deviceId = request.getParameter(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME);
        if (StringUtils.isEmpty(deviceId)) {
            deviceId = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);
        }
        if (StringUtils.isEmpty(deviceId)) {
            deviceId = request.getRequestedSessionId();
        }

        result.getData().put(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME, deviceId);

        result.getData().put(DEFAULT_VERSION_NAME, nacosDiscoveryProperties.getMetadata().get(DEFAULT_VERSION_NAME));
        result.getData().put(DEFAULT_SERVICES_NAME, discoveryClient.getServices());
        result.getData().put(DEFAULT_PLUGIN_NAME, pluginResourceService.getPluginServerNames());

        return result;
    }

    /**
     * 创建 reset 结果集
     *
     * @return reset 结果集
     */
    private RestResult<Map<String, Object>> createRestResult(HttpServletRequest request) {

        String executeCode = String.valueOf(HttpStatus.OK.value());
        String message = HttpStatus.OK.getReasonPhrase();
        int status = HttpStatus.OK.value();

        Map<String, Object> data = new LinkedHashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass()) ||
                AnonymousUser.class.isAssignableFrom(authentication.getDetails().getClass())) {
            data.put(DEFAULT_AUTHENTICATED_NAME, false);
            message = HttpStatus.UNAUTHORIZED.getReasonPhrase();
        } else {
            data.put(DEFAULT_AUTHENTICATED_NAME, authentication.isAuthenticated());

            if (!authentication.isAuthenticated()) {
                message = HttpStatus.UNAUTHORIZED.getReasonPhrase();
            }

            if (PrincipalAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
                PrincipalAuthenticationToken authenticationToken = Casts.cast(authentication);
                data.put(RememberMeProperties.DEFAULT_PARAM_NAME, authenticationToken.isRememberMe());

                if (authenticationToken.isRememberMe()) {
                    data.put(RememberMeProperties.DEFAULT_USER_DETAILS_NAME, authentication.getDetails());
                }
            }
        }

        String identified = Objects.toString(
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME),
                UUID.randomUUID().toString()
        );

        data.put(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME, identified);

        return new RestResult<>(
                message,
                status,
                executeCode,
                data
        );
    }

    /**
     * 验证码数据处理
     *
     * @param result  reset 结果集
     * @param request http 请求信息
     */
    private void postCaptchaData(RestResult<Map<String, Object>> result, HttpServletRequest request) {

        IdValueMeta<String, Map<String, Object>> meta = failureHandler.getAllowableFailureMeta(request);
        Integer number = Casts.cast(meta.getValue().getOrDefault(AuthenticationFailureResponse.ALLOWABLE_FAILURE_NUMBER_NAME, 0));

        Integer allowableFailureNumber = applicationConfig.getAllowableFailureNumber();

        if (number >= allowableFailureNumber) {
            String captchaType = applicationConfig.getFormLoginFailureCaptchaType();

            Map<String, Object> buildToken = failureHandler
                    .getResourceServiceFeignClient()
                    .createCaptchaToken(captchaType, meta.getId(), new LinkedHashMap<>());

            result.getData().put(SystemConstants.CAPTCHA_TOKEN_NAME, buildToken);
            result.setExecuteCode(SystemErrorCodeConstants.CAPTCHA_EXECUTE_CODE);
        }
    }
}
