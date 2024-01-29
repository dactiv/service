package com.github.dactiv.service.commons.service.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 主数据比对文件类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum OssTypeEnum implements NameValueEnum<Integer> {

    MINIO("minio", 10),

    ;

    private final String name;

    private final Integer value;


}
