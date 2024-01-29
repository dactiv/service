package com.github.dactiv.service.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * app 类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SsoApplicationTypeEnum implements NameValueEnum<Integer> {

    /**
     * PC 端
     */
    PC(10, "PC 端"),

    /**
     * 移动端
     */
    MOBILE(20, "移动端"),

    ;

    private final Integer value;

    private final String name;
}
