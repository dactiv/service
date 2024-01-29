package com.github.dactiv.service.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

/**
 * oauth 认证类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthorizationGrantTypeEnum implements NameEnum {

    /**
     * 刷新令牌
     */
    REFRESH_TOKEN("刷新令牌", AuthorizationGrantType.REFRESH_TOKEN, true),

    /**
     * 授权代码
     */
    AUTHORIZATION_CODE("授权代码", AuthorizationGrantType.AUTHORIZATION_CODE, true),

    /**
     * 客户端令牌
     */
    CLIENT_CREDENTIALS("客户端令牌", AuthorizationGrantType.CLIENT_CREDENTIALS, false),

    ;


    private final String name;

    private final AuthorizationGrantType value;

    private final boolean isDefault;

    public static AuthorizationGrantTypeEnum ofValue(AuthorizationGrantType value) {
        Assert.notNull(value, "AuthorizationGrantTypeEnum 的 ofValue 方法的 value 不能为空");
        for (AuthorizationGrantTypeEnum e : AuthorizationGrantTypeEnum.values()) {
            if (e.getValue().getValue().equals(value.getValue())) {
                return e;
            }
        }
        return null;
    }
}
