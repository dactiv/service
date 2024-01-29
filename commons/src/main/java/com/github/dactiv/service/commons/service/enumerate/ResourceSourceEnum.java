package com.github.dactiv.service.commons.service.enumerate;

import com.github.dactiv.framework.commons.annotation.GetValueStrategy;
import com.github.dactiv.framework.commons.annotation.IgnoreField;
import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import com.github.dactiv.framework.commons.exception.SystemException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 插件来源枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@GetValueStrategy(type = GetValueStrategy.Type.ToString)
public enum ResourceSourceEnum implements NameValueEnum<String> {

    /**
     * 管理后台用户
     */
    CONSOLE("管理后台用户", ResourceSourceEnum.CONSOLE_SOURCE_VALUE),

    /**
     * 医联体用户
     */
    HEALTHCARE_ALLIANCE("医联体用户", ResourceSourceEnum.HEALTHCARE_ALLIANCE_VALUE),

    /**
     * 东盟用户
     */
    ASEAN("东盟用户", ResourceSourceEnum.ASEAN_VALUE),

    /**
     * 系统
     */
    @IgnoreField
    SYSTEM("系统", ResourceSourceEnum.SYSTEM_SOURCE_VALUE),

    ;

    /**
     * 中文名称
     */
    private final String name;

    /**
     * 值
     */
    private final String value;

    /**
     * 管理后台应用来源值
     */
    public static final String CONSOLE_SOURCE_VALUE = "CONSOLE";

    /**
     * 匿名用户应用来源值
     */
    public static final String ANONYMOUS_USER_SOURCE_VALUE = "ANONYMOUS_USER";

    /**
     * 系统应用来源值
     */
    public static final String SYSTEM_SOURCE_VALUE = "SYSTEM";

    /**
     * 医联体用户应用来源值
     */
    public static final String HEALTHCARE_ALLIANCE_VALUE = "HEALTHCARE_ALLIANCE";

    /**
     * 东盟用户应用来源
     */
    public static final String ASEAN_VALUE = "ASEAN";

    public static final List<String> SSO_USER_SOURCE = Arrays.asList(HEALTHCARE_ALLIANCE_VALUE, ASEAN_VALUE);

    public static ResourceSourceEnum parse(String value) {
        for (ResourceSourceEnum resourceSource : ResourceSourceEnum.values()) {
            if (resourceSource.getValue().contains(value)) {
                return resourceSource;
            }
        }
        throw new SystemException("找不到 [" + value + "] 对应的 ResourceSourceEnum 枚举实例");
    }

}
