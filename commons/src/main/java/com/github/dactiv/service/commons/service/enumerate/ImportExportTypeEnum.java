package com.github.dactiv.service.commons.service.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导入导出类型美剧
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ImportExportTypeEnum implements NameValueEnum<String> {

    /**
     * 后台系统用户
     */
    CONSOLE_USER("console_user", "后台系统用户"),

    ;

    private final String value;

    private final String name;
}
