package com.github.dactiv.service.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;

import java.io.Serial;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * <p>Table: tb_authorization_consent - 商户授权同意用户信息</p>
 *
 * @author maurice.chen
 * @since 2023-11-28 03:46:21
 */
@Data
@NoArgsConstructor
@Alias("authorizationConsent")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_authorization_consent", autoResultMap = true)
public class AuthorizationConsentEntity extends BasicUserDetails<Integer> implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = -7583569400043931473L;

    public static final String AUTHORITIES_SCOPE_PREFIX = "SCOPE_";

    private Integer id;

    private Date creationTime = new Date();

    @Version
    private Integer version;
    /**
     * 权限信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<String> authorities;

    /**
     * 对应的商户 id
     */
    private String merchantClientId;

    /**
     * 最后同意时间
     */
    private Date lastConsentTime;

    /**
     * 过期时间
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private TimeProperties expirationTime;

    public static AuthorizationConsentEntity ofAuthorizationConsent(OAuth2AuthorizationConsent authorizationConsent) {
        AuthorizationConsentEntity entity = new AuthorizationConsentEntity();
        entity.setMerchantClientId(authorizationConsent.getRegisteredClientId());

        List<String> authorities = authorizationConsent
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .collect(Collectors.toList());
        entity.setAuthorities(authorities);

        return entity;
    }

    public boolean isExpired() {
        if (Objects.isNull(expirationTime)) {
            return false;
        }

        if (Objects.nonNull(lastConsentTime)) {
            return lastConsentTime.getTime() + expirationTime.toMillis() < System.currentTimeMillis();
        } else {
            return creationTime.getTime() + expirationTime.toMillis() < System.currentTimeMillis();
        }
    }

    public Set<String> getScopes() {
        return toAuthorizationConsent().getScopes();
    }

    public OAuth2AuthorizationConsent toAuthorizationConsent() {
        String principalName = getUserType() + CacheProperties.DEFAULT_SEPARATOR + getUsername();
        return OAuth2AuthorizationConsent.withId(getMerchantClientId(), principalName)
                .authorities(authorities -> getAuthorities().stream().map(SimpleGrantedAuthority::new).forEach(authorities::add))
                .build();
    }
}