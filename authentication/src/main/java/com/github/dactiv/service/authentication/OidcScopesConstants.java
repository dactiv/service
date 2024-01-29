package com.github.dactiv.service.authentication;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.config.CommonsConfig;
import com.github.dactiv.service.commons.service.domain.meta.TypeIdNameMeta;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.util.Map;
import java.util.Set;

/**
 * oidc 扩展作用域常量
 *
 * @author maurice.chen
 */
public interface OidcScopesConstants {

    /**
     * 系统唯一识别
     */
    String UNION_ID = "unionId";


    static void createOidcUserInfoClaims(OAuth2Authorization oAuth2Authorization,
                                         Map<String, Object> claims,
                                         CommonsConfig commonsConfig,
                                         String appKey,
                                         SecurityUserDetails userDetails) {
        Set<String> scopes = oAuth2Authorization.getAuthorizedScopes();

        // 授权邮箱
        if (scopes.contains(OidcScopes.EMAIL) && userDetails.getMeta().containsKey(SecurityUserDetailsConstants.EMAIL_KEY)) {
            String email = userDetails.getMeta().getOrDefault(SecurityUserDetailsConstants.EMAIL_KEY, StringUtils.EMPTY).toString();
            email = commonsConfig.decrypt(email);
            String cipherText = commonsConfig.encrypt(email, appKey);
            claims.put(StandardClaimNames.EMAIL, cipherText);
            claims.put(StandardClaimNames.EMAIL_VERIFIED, userDetails.getMeta().get(SecurityUserDetailsConstants.EMAIL_VERIFIED_KEY));
        }

        // 授权电话号码
        if (scopes.contains(OidcScopes.PHONE) && userDetails.getMeta().containsKey(SecurityUserDetailsConstants.PHONE_NUMBER_KEY)) {
            String phone = userDetails.getMeta().getOrDefault(SecurityUserDetailsConstants.PHONE_NUMBER_KEY, StringUtils.EMPTY).toString();
            phone = commonsConfig.decrypt(phone);
            String cipherText = commonsConfig.encrypt(phone, appKey);
            claims.put(StandardClaimNames.PHONE_NUMBER, cipherText);
            claims.put(StandardClaimNames.PHONE_NUMBER_VERIFIED, userDetails.getMeta().get(SecurityUserDetailsConstants.PHONE_NUMBER_VERIFIED_KEY));
        }

        // 授权 openid
        if (scopes.contains(OidcScopes.OPENID)) {
            String clientId = oAuth2Authorization.getRegisteredClientId();
            String userUniqueValue = clientId
                    + CacheProperties.DEFAULT_SEPARATOR
                    + userDetails.toBasicUserDetails().toUniqueValue();
            String cipherText = commonsConfig.encrypt(
                    userUniqueValue,
                    appKey
            );
            claims.put(OidcScopes.OPENID, cipherText);
        }

        // 授权 union_id
        if (scopes.contains(OidcScopesConstants.UNION_ID)) {
            String userUniqueValue = userDetails.toBasicUserDetails().toUniqueValue();
            String cipherText = commonsConfig.encrypt(
                    userUniqueValue,
                    appKey
            );
            claims.put(OidcScopesConstants.UNION_ID, cipherText);
        }

        // 授权个人信息
        if (scopes.contains(OidcScopes.PROFILE) && MapUtils.isNotEmpty(userDetails.getProfile())) {
            claims.put(OidcScopes.PROFILE, userDetails.getProfile());
        }

        claims.put(SecurityUserDetailsConstants.USERNAME_KEY, userDetails.getUsername());
        claims.put(TypeIdNameMeta.TYPE_FIELD_NAME, userDetails.getType());
        claims.put(RestResult.DEFAULT_STATUS_NAME, Casts.convertValue(userDetails.getStatus(), Map.class));
    }

}
