package com.github.dactiv.service.authentication.resolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.spring.security.authentication.oidc.OidcUserInfoAuthenticationResolver;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.authentication.OidcScopesConstants;
import com.github.dactiv.service.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.domain.meta.TypeIdNameMeta;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 系统用户解析器
 *
 * @author maurice.chen
 */
public interface SystemUserResolver extends OidcUserInfoAuthenticationResolver {

    /**
     * 转换 spring security 用户实现为系统用户
     *
     * @param userDetails spring security 用户实现
     * @return 系统用户
     */
    SystemUserEntity convertTargetUser(TypeUserDetails<Integer> userDetails);

    /**
     * 获取所有用户信息
     *
     * @param ids 主键 id 集合
     * @return 所有用户
     */
    List<SystemUserEntity> getUserByGroupId(List<Integer> ids);

    /**
     * 通过唯一识别获取系统用户
     *
     * @param identity 唯一识别
     * @return 系统用户
     */
    SystemUserEntity getByIdentity(String identity);

    /**
     * 更新数据
     *
     * @param user 用户信息
     */
    void update(SystemUserEntity user);

    /**
     * 新增数据
     *
     * @param user 用户信息
     */
    void insert(SystemUserEntity user);

    /**
     * 更新或保存数据
     *
     * @param user 用户信息
     */
    void save(SystemUserEntity user);


    @Override
    default OidcUserInfo mappingOidcUserInfoClaims(OAuth2Authorization oAuth2Authorization, Map<String, Object> claims, SecurityUserDetails userDetails) {
        createOidcUserInfo(oAuth2Authorization, claims, userDetails);
        return new OidcSecurityUserDetailsInfo(claims);
    }

    /**
     * 创建 oidc 用户信息
     *
     * @param oAuth2Authorization oauth2 授权信息
     * @param claims              当前的 claims
     * @param userDetails         当前用户明细
     */
    void createOidcUserInfo(OAuth2Authorization oAuth2Authorization, Map<String, Object> claims, SecurityUserDetails userDetails);

    /**
     * 根据登陆的用户 SecurityUserDetails 构造的 oidc 响应体实现
     *
     * @author maurice.chen
     */
    @JsonIgnoreProperties({"claims", "fullName"})
    class OidcSecurityUserDetailsInfo extends OidcUserInfo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1163307879932770226L;

        public OidcSecurityUserDetailsInfo(Map<String, Object> claims) {
            super(claims);
        }

        @Override
        @JsonIgnore
        public String getProfile() {
            return Casts.writeValueAsString(getClaimAsMap(StandardClaimNames.PROFILE));
        }

        @JsonProperty("profile")
        public Map<String, Object> getProfileMetadata() {
            return getClaimAsMap(StandardClaimNames.PROFILE);
        }

        public String getUsername() {
            return getClaimAsString(SecurityUserDetailsConstants.USERNAME_KEY);
        }

        public String getOpenid() {
            return getClaimAsString(OidcScopes.OPENID);
        }

        public String getUnionId() {
            return getClaimAsString(OidcScopesConstants.UNION_ID);
        }

        public String getType() {
            return getClaimAsString(TypeIdNameMeta.TYPE_FIELD_NAME);
        }

        public Map<String, Object> getStatus() {
            return getClaimAsMap(RestResult.DEFAULT_STATUS_NAME);
        }
    }
}
