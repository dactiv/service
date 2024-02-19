package com.github.dactiv.service.authentication.security;

import com.github.dactiv.framework.captcha.filter.CaptchaVerificationInterceptor;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.service.authentication.security.handler.AuthenticationFailureResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 针对登陆 url 是否需要验证码校验的断言实现，该类作用为系统可能存在登陆错误N次之后才需要校验验证码，
 * 但配置里又将 /login url 为需要验证码校验通过后才能访问，所以在这里处理为如果需要验证码校验才进入到
 * {@link com.github.dactiv.framework.captcha.filter.CaptchaVerificationFilter}
 * 进行校验，否则直接掠过该过滤器。
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class AuthenticationCaptchaVerificationInterceptor implements CaptchaVerificationInterceptor {

    private final AuthenticationFailureResponse authenticationFailureResponse;

    @Override
    public boolean preVerify(HttpServletRequest request) {
        String url = authenticationFailureResponse.getSpringSecurityProperties().getLoginProcessingUrl();
        AntPathRequestMatcher antPathRequestMatcher = new AntPathRequestMatcher(url);
        if (!antPathRequestMatcher.matches(request)) {
            return false;
        }

        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            return true;
        }

        return !authenticationFailureResponse.isCaptchaAuthentication(request);
    }

    @Override
    public void exceptionVerify(HttpServletRequest request, RestResult<Map<String, Object>> result, Exception e) {
        String url = authenticationFailureResponse.getSpringSecurityProperties().getLoginProcessingUrl();
        AntPathRequestMatcher antPathRequestMatcher = new AntPathRequestMatcher(url);
        if (!antPathRequestMatcher.matches(request)) {
            return;
        }
        authenticationFailureResponse.setting(result, request, new BadCredentialsException(e.getMessage()));
    }
}
