package com.github.dactiv.service.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.service.authentication.OidcScopesConstants;
import lombok.*;
import org.springframework.security.oauth2.core.oidc.OidcScopes;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * oauth2 注册客户端作用域
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RegisteredClientScopeEnum implements NameValueEnum<String> {

    PROFILE("个人资料", OidcScopes.PROFILE, true, "此应用程序将能够读取您的个人资料信息。"),

    OPENID("open id", OidcScopes.OPENID, true, "此应用程序将能够为您的创建属于该应用程序的个人唯一识别信息。"),

    UNION_ID("union id", OidcScopesConstants.UNION_ID, true, "此应用程序将能够为读取您在本系统中的唯一识别信息。"),

    EMAIL("电子邮箱", OidcScopes.EMAIL, false, "此应用程序将能够读取您的电子邮箱信息。"),

    ADDRESS("个人地址", OidcScopes.ADDRESS, false, "此应用程序将能够读取您的个人住宅资料信息。"),

    PHONE("联系电话", OidcScopes.PHONE, false, "此应用程序将能够读取您的联系电话。"),

    ;

    private final String name;

    private final String value;

    private final boolean isDefault;

    private final String description;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Description implements Serializable {

        @Serial
        private static final long serialVersionUID = 6543998509887161155L;

        private String scope;

        private String description;

    }

    public Description toDescription() {
        return new Description(getValue(), getDescription());
    }

    public Description toDescription(String description) {
        return new Description(getValue(), Objects.toString(description, getDescription()));
    }

}
