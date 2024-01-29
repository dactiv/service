package com.github.dactiv.service.authentication.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.service.authentication.dao.AuthorizationConsentDao;
import com.github.dactiv.service.authentication.domain.entity.AuthorizationConsentEntity;
import com.github.dactiv.service.authentication.domain.entity.MerchantClientEntity;
import com.github.dactiv.service.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * tb_authorization_consent 的业务逻辑
 *
 * <p>Table: tb_authorization_consent - 商户授权同意用户信息</p>
 *
 * @author maurice.chen
 * @see AuthorizationConsentEntity
 * @since 2023-11-28 03:46:21
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.NOT_SUPPORTED)
public class AuthorizationConsentService extends BasicService<AuthorizationConsentDao, AuthorizationConsentEntity> implements OAuth2AuthorizationConsentService {

    private final AuthorizationService authorizationService;

    private final MerchantClientService merchantClientService;

    public AuthorizationConsentEntity getByUniqueIndex(ResourceSourceEnum source, String username, String merchantClientId) {
        return lambdaQuery()
                .eq(BasicUserDetails::getUserType, source.toString())
                .eq(BasicUserDetails::getUsername, username)
                .eq(AuthorizationConsentEntity::getMerchantClientId, merchantClientId)
                .one();
    }

    public AuthorizationConsentEntity getByUniqueIndex(String merchantClientId, String principalName) {
        BasicUserDetails<Integer> userDetails = new BasicUserDetails<>();

        String[] principal = StringUtils.splitByWholeSeparator(principalName, CacheProperties.DEFAULT_SEPARATOR);
        userDetails.setUserType(principal[0]);
        userDetails.setUsername(principal[1]);

        return getByUniqueIndex(ResourceSourceEnum.valueOf(userDetails.getUserType()), userDetails.getUsername(), merchantClientId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OAuth2AuthorizationConsent authorizationConsent) {

        BasicUserDetails<Integer> userDetails = new BasicUserDetails<>();

        String[] principal = StringUtils.splitByWholeSeparator(authorizationConsent.getPrincipalName(), CacheProperties.DEFAULT_SEPARATOR);
        userDetails.setUserType(principal[0]);
        userDetails.setUsername(principal[1]);

        AuthorizationConsentEntity exist = getByUniqueIndex(ResourceSourceEnum.valueOf(userDetails.getUserType()), userDetails.getUsername(), authorizationConsent.getRegisteredClientId());
        MerchantClientEntity merchantClient = merchantClientService.get(authorizationConsent.getRegisteredClientId());

        if (Objects.isNull(exist)) {
            SystemUserEntity user = authorizationService.getSystemUser(userDetails);
            Assert.notNull(user, "找不到类型为 [" + userDetails.getUserType() + "] 登陆账号为 [" + userDetails.getUsername() + "] 的系统用户");

            userDetails.setUserId(user.getId());

            AuthorizationConsentEntity entity = AuthorizationConsentEntity.ofAuthorizationConsent(authorizationConsent);

            entity.setExpirationTime(merchantClient.getClientSettings().getAuthorizationConsentExpirationTime());
            entity.setUserDetails(userDetails);
            insert(entity);
        } else {
            List<String> authorities = authorizationConsent
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .distinct()
                    .collect(Collectors.toList());
            exist.setAuthorities(authorities);
            exist.setLastConsentTime(new Date());
            exist.setExpirationTime(merchantClient.getClientSettings().getAuthorizationConsentExpirationTime());
            updateById(exist);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        AuthorizationConsentEntity entity = getByUniqueIndex(authorizationConsent.getRegisteredClientId(), authorizationConsent.getPrincipalName());
        if (Objects.isNull(entity)) {
            return;
        }
        deleteByEntity(entity);
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        AuthorizationConsentEntity entity = getByUniqueIndex(registeredClientId, principalName);

        if (Objects.isNull(entity)) {
            return null;
        }

        if (entity.isExpired()) {
            deleteById(entity);
            return null;
        }

        return entity.toAuthorizationConsent();
    }
}
