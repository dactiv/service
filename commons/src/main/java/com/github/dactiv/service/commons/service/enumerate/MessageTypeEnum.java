package com.github.dactiv.service.commons.service.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MessageTypeEnum implements NameValueEnum<Integer> {

    /**
     * 通知
     */
    NOTICE(10, "通知"),

    /**
     * 警告
     */
    WARNING(30, "警告消息"),

    /**
     * 系统
     */
    SYSTEM(40, "系统消息"),
    ;

    /**
     * 值
     */
    private final Integer value;

    /**
     * 名称
     */
    private final String name;

}
