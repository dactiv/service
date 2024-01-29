package com.github.dactiv.service.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;

/**
 * oauth 认证方法枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ClientAuthenticationMethodEnum implements NameEnum {

    /**
     * 客户端密钥 basic 认证
     */
    CLIENT_SECRET_BASIC("客户端密钥 basic 认证", ClientAuthenticationMethod.CLIENT_SECRET_BASIC, true),

    /**
     * 客户端密钥 post 认证
     */
    CLIENT_SECRET_POST("客户端密钥 post 认证", ClientAuthenticationMethod.CLIENT_SECRET_POST, true),

    /**
     * 客户端私有 key jwt 认证
     */
    PRIVATE_KEY_JWT("客户端私有 key jwt 认证", ClientAuthenticationMethod.PRIVATE_KEY_JWT, false),

    /**
     * 客户端 jwt 认证
     */
    CLIENT_SECRET_JWT("客户端密钥 post 认证", ClientAuthenticationMethod.CLIENT_SECRET_JWT, false),

    ;


    private final String name;

    private final ClientAuthenticationMethod value;

    private final boolean isDefault;

    public static ClientAuthenticationMethodEnum ofValue(ClientAuthenticationMethod value) {
        Assert.notNull(value, "ClientAuthenticationMethodEnum 的 ofValue 方法的 value 不能为空");
        for (ClientAuthenticationMethodEnum e : ClientAuthenticationMethodEnum.values()) {
            if (e.getValue().getValue().equals(value.getValue())) {
                return e;
            }
        }
        return null;
    }
}
