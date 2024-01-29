package com.github.dactiv.service.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * app 环境枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SsoApplicationEnvironmentEnum implements NameValueEnum<Integer> {

    /**
     * 开发环境
     */
    DEVELOP(10, "开发环境"),

    /**
     * 测试环境
     */
    TEST(20, "测试环境"),

    /**
     * 生产环境
     */
    PRODUCE(30, "生产环境"),

    ;

    private final Integer value;

    private final String name;
}
