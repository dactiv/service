package com.github.dactiv.service.message.domain.meta.sms;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信预处理元数据细腻
 *
 * @author maurice.chen
 */
@Data
public class SmsConfigPrepareMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 3720622137180474753L;

    /**
     * 发送短信验证码类型
     */
    private String sendCaptchaType = "tianai";
}
