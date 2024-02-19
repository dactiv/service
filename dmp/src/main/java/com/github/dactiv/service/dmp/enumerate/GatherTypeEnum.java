package com.github.dactiv.service.dmp.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采集类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GatherTypeEnum implements NameValueEnum<String> {

    TENCENT_MAP("tencentMap", "腾讯地图"),

    ALIBABA_MAP("alibabaMap", "高德地图"),

    BAIDU_MAP("baiduMap", "百度地图"),

    ;

    private final String value;

    private final String name;
}
