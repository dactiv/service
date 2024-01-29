package com.github.dactiv.service.commons.service.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * 时间单位枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TimeUnitEnum implements NameValueEnum<String> {

    DAYS("天", TimeUnit.DAYS.toString()),

    HOURS("小时", TimeUnit.HOURS.toString()),

    MINUTES("分钟", TimeUnit.MINUTES.toString()),

    SECONDS("秒", TimeUnit.SECONDS.toString()),

    MILLISECONDS("毫秒", TimeUnit.MILLISECONDS.toString()),

    MICROSECONDS("微秒", TimeUnit.MICROSECONDS.toString()),

    NANOSECONDS("纳秒", TimeUnit.NANOSECONDS.toString()),

    ;

    private final String name;

    private final String value;

    public TimeUnit toTimeUnit() {
        return TimeUnit.valueOf(getName());
    }

}
