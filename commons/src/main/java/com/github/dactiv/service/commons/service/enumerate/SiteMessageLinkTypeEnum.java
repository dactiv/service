package com.github.dactiv.service.commons.service.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息链接类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SiteMessageLinkTypeEnum implements NameValueEnum<String> {

    /**
     * 认证信息
     */
    AUTHENTICATION_INFO("认证信息", "authentication.info"),

    ;

    /**
     * 名称
     */
    private final String name;
    /**
     * 值
     */
    private final String value;

}
