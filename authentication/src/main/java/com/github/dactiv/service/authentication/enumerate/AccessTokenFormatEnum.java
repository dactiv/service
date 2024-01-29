package com.github.dactiv.service.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;

import java.util.Objects;

/**
 * 访问令牌格式化配置
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AccessTokenFormatEnum implements NameValueEnum<Integer> {

    SELF_CONTAINED("独立的", 10, OAuth2TokenFormat.SELF_CONTAINED),

    REFERENCE("参考文献", 20, OAuth2TokenFormat.REFERENCE),

    ;


    private final String name;

    private final Integer value;

    private final OAuth2TokenFormat format;

    public static AccessTokenFormatEnum ofTokenFormat(OAuth2TokenFormat tokenFormat) {
        if (Objects.isNull(tokenFormat)) {
            return null;
        }

        for (AccessTokenFormatEnum formatEnum : values()) {
            if (formatEnum.getFormat().getValue().equals(tokenFormat.getValue())) {
                return formatEnum;
            }
        }

        return null;
    }
}
