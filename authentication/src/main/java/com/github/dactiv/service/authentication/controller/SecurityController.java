package com.github.dactiv.service.authentication.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.security.audit.PluginAuditEvent;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.OAuth2Properties;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.authentication.enumerate.RegisteredClientScopeEnum;
import com.github.dactiv.service.authentication.security.handler.CaptchaAuthenticationSuccessResponse;
import com.github.dactiv.service.authentication.security.handler.JsonLogoutSuccessHandler;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 安全控制器
 *
 * @author maurice.chen
 */
@Controller
@RequiredArgsConstructor
public class SecurityController {

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    private final OAuth2Properties oAuth2Properties;

    private final CaptchaAuthenticationSuccessResponse captchaAuthenticationSuccessResponse;

    private final RegisteredClientRepository registeredClientRepository;

    private final List<UserDetailsService> userDetailsServices;

    /**
     * 登录预处理
     *
     * @param request http servlet request
     * @return rest 结果集
     */
    @ResponseBody
    @GetMapping("prepare")
    public RestResult<Map<String, Object>> prepare(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * 用户登录
     *
     * @return 未授权访问结果
     */
    @ResponseBody
    @GetMapping("login")
    public RestResult<Map<String, Object>> login(HttpServletRequest request) {
        return jsonLogoutSuccessHandler.createUnauthorizedResult(request);
    }

    /**
     * 登录成功后跳转的连接，直接获取当前用户
     *
     * @param securityContext 安全上下文
     * @return 当前用户
     */
    @ResponseBody
    @GetMapping("getPrincipal")
    @PreAuthorize("isAuthenticated()")
    public SecurityUserDetails getPrincipal(@CurrentSecurityContext SecurityContext securityContext) {
        Object details = securityContext.getAuthentication().getDetails();
        if (!SecurityUserDetails.class.isAssignableFrom(details.getClass())) {
            return null;
        }

        SecurityUserDetails userDetails = Casts.cast(details);
        // 由于 userDetails 是引用类型，更新后可能 session 值会发生
        // 变更导致可能 redis 和当前 session 值不一致，复制一个新的做响应数据
        SecurityUserDetails returnValue = Casts.of(userDetails, SecurityUserDetails.class);
        captchaAuthenticationSuccessResponse.postSecurityUserDetails(returnValue);

        return returnValue;
    }

    @ResponseBody
    @GetMapping(value = "/oauth2/consent")
    public Map<String, Object> consent(@CurrentSecurityContext SecurityContext securityContext,
                                       @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                                       @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
                                       @RequestParam(OAuth2ParameterNames.STATE) String state) {
        Map<String, Object> result = new LinkedHashMap<>();

        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        Assert.notNull(registeredClient, "找不到 ID 为 [" + clientId + "] 的商户客户端信息");
        Authentication authentication = securityContext.getAuthentication();

        SecurityUserDetails userDetails = Casts.cast(authentication.getDetails());

        List<String> requestedScopes = new LinkedList<>(Arrays.asList(StringUtils.splitByWholeSeparator(scope, StringUtils.SPACE)));

        List<RegisteredClientScopeEnum.Description> scopes = requestedScopes
                .stream()
                .map(s -> ValueEnumUtils.parse(s, RegisteredClientScopeEnum.class, true))
                .filter(Objects::nonNull)
                .map(RegisteredClientScopeEnum::toDescription)
                .collect(Collectors.toList());

        result.put(OAuth2ParameterNames.CLIENT_ID, clientId);
        result.put(OAuth2ParameterNames.STATE, state);
        result.put(OAuth2ParameterNames.SCOPE, scopes);
        result.put(PluginAuditEvent.PRINCIPAL_FIELD_NAME, userDetails.toBasicUserDetails());

        String consentPageUri = StringUtils.prependIfMissing(oAuth2Properties.getConsentPageUri(), AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        result.put(OAuth2ParameterNames.RESPONSE_TYPE, SystemConstants.SYS_AUTHENTICATION_NAME + consentPageUri);

        return result;
    }

    /**
     * 更新系统用户登录密码
     *
     * @param securityContext 安全上下文
     * @param oldPassword     旧密码
     * @param newPassword     新密码
     */
    @PostMapping("updatePassword")
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "修改个人登录密码", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true, operationDataTrace = true, parent = "authority")
    public RestResult<?> updatePassword(@CurrentSecurityContext SecurityContext securityContext,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        userDetailsServices
                .stream()
                .filter(s -> s.getType().contains(userDetails.getType()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + userDetails.getType() + "] 的系统用户解析器实现"))
                .updatePassword(userDetails, oldPassword, newPassword);

        return RestResult.of("修改密码成功");
    }

    /**
     * 更新系统用户登录密码
     *
     * @param type 用户类型
     * @param id   用户 id
     */
    @PostMapping("adminRestPassword")
    @PreAuthorize("hasAuthority('perms[system_user:admin_reset_password]')")
    @Plugin(name = "重置用户密码", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true, operationDataTrace = true, parent = "authority")
    public RestResult<String> adminRestPassword(String type, Integer id) {

        SecurityUserDetails userDetails = Casts.cast(type);

        String newPassword = userDetailsServices
                .stream()
                .filter(s -> s.getType().contains(userDetails.getType()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + userDetails.getType() + "] 的系统用户解析器实现"))
                .adminRestPassword(id);

        return RestResult.ofSuccess("重置密码成功", newPassword);
    }

}
