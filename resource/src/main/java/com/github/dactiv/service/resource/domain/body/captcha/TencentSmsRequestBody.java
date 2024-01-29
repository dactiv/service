package com.github.dactiv.service.resource.domain.body.captcha;

import com.github.dactiv.service.commons.service.SystemConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 腾讯短信验证码请求体
 *
 * @author maurice.chen
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class TencentSmsRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = -3186684028604469278L;

    /**
     * 手机号码
     */
    @Pattern(regexp = SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION, message = "手机号码格式错误")
    @NotBlank(message = "手机号码不能为空")
    private String phoneNumber;
}
