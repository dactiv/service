package com.github.dactiv.service.commons.service.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 附件类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttachmentTypeEnum implements NameValueEnum<String> {

    /**
     * 课件附件
     */
    USER_FILE("user.file", "用户资源附件"),

    /**
     * 系统文件
     */
    SYSTEM_FILE("system.file", "系统文件"),

    /**
     * 临时文件
     */
    TEMP("temp", "临时文件附件"),

    /**
     * 验证码校验安全
     */
    CAPTCHA_VERIFY_FILE("captcha.verify.file", "验证码校验文件");

    private final String value;

    private final String name;
}
