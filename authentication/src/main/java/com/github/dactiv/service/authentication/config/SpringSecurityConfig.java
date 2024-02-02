package com.github.dactiv.service.authentication.config;

import com.github.dactiv.framework.spring.security.authentication.adapter.WebSecurityConfigurerAfterAdapter;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.service.authentication.security.JsonSessionInformationExpiredStrategy;
import com.github.dactiv.service.authentication.security.handler.JsonLogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Component;

/**
 * 自定义 spring security 的配置
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class SpringSecurityConfig implements WebSecurityConfigurerAfterAdapter {

    private final CookieRememberService rememberMeServices;

    private final JsonSessionInformationExpiredStrategy jsonSessionInformationExpiredStrategy;

    private final ApplicationConfig applicationConfig;

    private final SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry;

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    @Override
    public void configure(HttpSecurity httpSecurity) {

        try {

            httpSecurity
                    .logout(this::logoutConfig)
                    .rememberMe(this::rememberMeConfig)
                    .sessionManagement(this::sessionManagementConfig);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sessionManagementConfig(SessionManagementConfigurer<HttpSecurity> sessionManagementConfigurer) {
        sessionManagementConfigurer
                .maximumSessions(Integer.MAX_VALUE)
                .sessionRegistry(sessionBackedSessionRegistry)
                .expiredSessionStrategy(jsonSessionInformationExpiredStrategy);
    }

    private void rememberMeConfig(RememberMeConfigurer<HttpSecurity> rememberMeConfigurer) {
        rememberMeConfigurer
                .alwaysRemember(true)
                .rememberMeServices(rememberMeServices);
    }

    private void logoutConfig(LogoutConfigurer<HttpSecurity> logoutConfigurer) {
        logoutConfigurer
                .logoutUrl(applicationConfig.getLogoutUrl())
                .logoutSuccessHandler(jsonLogoutSuccessHandler);
    }
}
