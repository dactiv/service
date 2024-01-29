package com.github.dactiv.service.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 单点登陆应用组等级枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SsoGroupLevelEnum implements NameValueEnum<Integer> {

    /**
     * 门户级别
     */
    PORTAL(10, "门户"),

    /**
     * 应用分组级别
     */
    GROUP(20, "应用分组"),

    /**
     * 应用程序级别
     */
    APPLICATION(30, "应用程序"),

    ;

    private final Integer value;

    private final String name;
}
