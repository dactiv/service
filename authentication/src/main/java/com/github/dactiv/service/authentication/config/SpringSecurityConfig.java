package com.github.dactiv.service.authentication.config;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.security.SpringSecurityAutoConfiguration;
import com.github.dactiv.framework.spring.security.authentication.adapter.OAuth2AuthorizationConfigurerAdapter;
import com.github.dactiv.framework.spring.security.authentication.adapter.WebSecurityConfigurerAfterAdapter;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.service.authentication.security.JsonSessionInformationExpiredStrategy;
import com.github.dactiv.service.authentication.security.handler.JsonLogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.authentication.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationEndpointConfigurer;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * 自定义 spring security 的配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor(onConstructor_ = @Lazy)
@AutoConfigureAfter({SpringSecurityAutoConfiguration.class, RedisHttpSessionConfiguration.class})
public class SpringSecurityConfig implements WebSecurityConfigurerAfterAdapter, OAuth2AuthorizationConfigurerAdapter {
    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1";

    private final CookieRememberService rememberMeServices;

    private final JsonSessionInformationExpiredStrategy jsonSessionInformationExpiredStrategy;

    private final ApplicationConfig applicationConfig;

    @Lazy
    private final SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry;

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    @Override
    public void configAuthorizationEndpoint(OAuth2AuthorizationEndpointConfigurer authorizationEndpoint) {
        authorizationEndpoint.authenticationProviders(this::settingAuthenticationProviders);
    }

    private void settingAuthenticationProviders(List<AuthenticationProvider> authenticationProviders) {
        Optional<OAuth2AuthorizationCodeRequestAuthenticationProvider> optional = authenticationProviders
                .stream()
                .filter(o -> OAuth2AuthorizationCodeRequestAuthenticationProvider.class.isAssignableFrom(o.getClass()))
                .map(o -> Casts.cast(o, OAuth2AuthorizationCodeRequestAuthenticationProvider.class))
                .findFirst();

        if (optional.isEmpty()) {
            return;
        }
        OAuth2AuthorizationCodeRequestAuthenticationProvider authenticationProvider = optional.get();
        authenticationProvider.setAuthenticationValidator(OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_SCOPE_VALIDATOR.andThen(this::validateRedirectUri));
    }

    private void validateRedirectUri(OAuth2AuthorizationCodeRequestAuthenticationContext authenticationContext) {
        OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
                authenticationContext.getAuthentication();
        RegisteredClient registeredClient = authenticationContext.getRegisteredClient();

        String requestedRedirectUri = authorizationCodeRequestAuthentication.getRedirectUri();

        if (StringUtils.hasText(requestedRedirectUri)) {
            // ***** redirect_uri is available in authorization request

            UriComponents requestedRedirect = null;
            try {
                requestedRedirect = UriComponentsBuilder.fromUriString(requestedRedirectUri).build();
            } catch (Exception ignored) {
            }

            if (requestedRedirect == null || !registeredClient.getRedirectUris().contains(requestedRedirect.getHost())) {
                throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.REDIRECT_URI,
                        authorizationCodeRequestAuthentication, registeredClient);
            }

            if (SpringMvcUtils.isLoopbackAddress(requestedRedirect.getHost())) {
                // As per https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics-22#section-4.1.3
                // When comparing client redirect URIs against pre-registered URIs,
                // authorization servers MUST utilize exact string matching.
                throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.REDIRECT_URI,
                        authorizationCodeRequestAuthentication, registeredClient);
            } else {
                // As per https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-08#section-8.4.2
                // The authorization server MUST allow any port to be specified at the
                // time of the request for loopback IP redirect URIs, to accommodate
                // clients that obtain an available ephemeral port from the operating
                // system at the time of the request.
                boolean validRedirectUri = false;
                for (String registeredRedirectUri : registeredClient.getRedirectUris()) {
                    UriComponentsBuilder registeredRedirect = UriComponentsBuilder.fromUriString(registeredRedirectUri);
                    registeredRedirect.port(requestedRedirect.getPort());
                    if (registeredRedirect.build().toString().equals(requestedRedirect.getHost())) {
                        validRedirectUri = true;
                        break;
                    }
                }
                if (!validRedirectUri) {
                    throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.REDIRECT_URI,
                            authorizationCodeRequestAuthentication, registeredClient);
                }
            }

        } else {
            // ***** redirect_uri is NOT available in authorization request

            if (authorizationCodeRequestAuthentication.getScopes().contains(OidcScopes.OPENID) ||
                    registeredClient.getRedirectUris().size() != 1) {
                // redirect_uri is REQUIRED for OpenID Connect
                throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.REDIRECT_URI,
                        authorizationCodeRequestAuthentication, registeredClient);
            }
        }
    }

    private static void throwError(String errorCode, String parameterName,
                                   OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication,
                                   RegisteredClient registeredClient) {
        OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, ERROR_URI);
        throwError(error, parameterName, authorizationCodeRequestAuthentication, registeredClient);
    }

    private static void throwError(OAuth2Error error,
                                   String parameterName,
                                   OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication,
                                   RegisteredClient registeredClient) {

        String redirectUri = StringUtils.hasText(authorizationCodeRequestAuthentication.getRedirectUri()) ?
                authorizationCodeRequestAuthentication.getRedirectUri() :
                registeredClient.getRedirectUris().iterator().next();
        if (error.getErrorCode().equals(OAuth2ErrorCodes.INVALID_REQUEST) &&
                parameterName.equals(OAuth2ParameterNames.REDIRECT_URI)) {
            redirectUri = null;        // Prevent redirects
        }

        OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthenticationResult =
                new OAuth2AuthorizationCodeRequestAuthenticationToken(
                        authorizationCodeRequestAuthentication.getAuthorizationUri(), authorizationCodeRequestAuthentication.getClientId(),
                        (Authentication) authorizationCodeRequestAuthentication.getPrincipal(), redirectUri,
                        authorizationCodeRequestAuthentication.getState(), authorizationCodeRequestAuthentication.getScopes(),
                        authorizationCodeRequestAuthentication.getAdditionalParameters());
        authorizationCodeRequestAuthenticationResult.setAuthenticated(true);

        throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, authorizationCodeRequestAuthenticationResult);
    }

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

    @Bean
    public SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry(RedisTemplate<String, Object> redisTemplate) {
        return new SpringSessionBackedSessionRegistry<>(new RedisIndexedSessionRepository(redisTemplate));
    }
}
