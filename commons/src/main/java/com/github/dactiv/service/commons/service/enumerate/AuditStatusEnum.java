package com.github.dactiv.service.commons.service.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AuditStatusEnum implements NameValueEnum<Integer> {

    /**
     * 待审核
     */
    AUDITABLE("待审核", 10),

    /**
     * 审核通过
     */
    AGREED("审核通过", 20),

    /**
     * 审核不通过
     */
    DISAGREE("审核不通过", 30),

    /**
     * 拒绝审核
     */
    REJECTED("拒绝审核", 40),

    ;

    private final String name;

    private final Integer value;
}
