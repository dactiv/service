package com.github.dactiv.service.resource.domain.body.captcha;

import com.github.dactiv.framework.captcha.SimpleMessageType;
import com.github.dactiv.service.commons.service.SystemConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信验证码请求体
 *
 * @author maurice
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsRequestBody extends SimpleMessageType implements Serializable {

    @Serial
    private static final long serialVersionUID = 1235954873943241073L;

    /**
     * 手机号码
     */
    @Pattern(regexp = SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION, message = "手机号码格式错误")
    @NotBlank(message = "手机号码不能为空")
    private String phoneNumber;

}
