package com.github.dactiv.service.resource.domain.body.captcha;

import com.github.dactiv.framework.captcha.SimpleMessageType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 电子邮件验证码请求体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailRequestBody extends SimpleMessageType implements Serializable {

    @Serial
    private static final long serialVersionUID = 3429703228723485142L;
    /**
     * 电子邮件
     */
    @Email(message = "电子邮件格式不正确")
    @NotBlank(message = "电子邮件不能为空")
    private String email;
}
