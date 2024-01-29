package com.github.dactiv.service.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型
 *
 * @author maurice
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuthenticationTypeEnum implements NameEnum {

    /**
     * 登录账户与密码登录
     */
    USERNAME("登录账户与密码登录"),

    /**
     * 手机号码
     */
    PHONE_NUMBER("手机号码登陆"),

    /**
     * 企业微信
     */
    WORK_WEI_XIN("企业微信"),

    ;

    private final String name;
}
