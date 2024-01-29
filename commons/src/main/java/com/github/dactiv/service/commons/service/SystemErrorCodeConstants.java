package com.github.dactiv.service.commons.service;

/**
 * 系统错误代码常量
 *
 * @author maurice.chen
 */
public interface SystemErrorCodeConstants {
    /**
     * 需要展示验证码错误代码
     */
    String CAPTCHA_EXECUTE_CODE = "10001";

    /**
     * 需要跳转到登陆的错误代码
     */
    String LOGIN_EXECUTE_CODE = "10002";

    /**
     * 内容已存在
     */
    String CONTENT_EXIST = "10400";

    /**
     * 内容不存在
     */
    String CONTENT_NOT_EXIST = "10404";

    /**
     * 未绑定手机号码
     */
    String PHONE_NUMBER_UNBINDING_CODE = "10003";
}
