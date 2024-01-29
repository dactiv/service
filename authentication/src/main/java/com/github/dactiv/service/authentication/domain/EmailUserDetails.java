package com.github.dactiv.service.authentication.domain;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;

import java.io.Serializable;

/**
 * 邮箱用户明细
 *
 * @author maurice.chen
 */
public interface EmailUserDetails extends Serializable {

    /**
     * 获取电子邮箱
     *
     * @return 电子邮箱
     */
    String getEmail();

    /**
     * 是否已验证码邮箱
     *
     * @return 是或否枚举
     */
    YesOrNo getEmailVerified();
}
