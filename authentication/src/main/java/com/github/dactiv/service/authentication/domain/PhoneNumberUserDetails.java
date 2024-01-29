package com.github.dactiv.service.authentication.domain;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;

import java.io.Serializable;

/**
 * 带手机号码的用户信息
 *
 * @author maurice.chen
 */
public interface PhoneNumberUserDetails extends Serializable {

    /**
     * 获取手机号码
     *
     * @return 手机号码
     */
    String getPhoneNumber();

    /**
     * 获取是否已验证码手机号码
     *
     * @return 是或否枚举
     */
    YesOrNo getPhoneNumberVerified();

    void setPhoneNumber(String phoneNumber);

    void setPhoneNumberVerified(YesOrNo phoneNumberVerified);
}
