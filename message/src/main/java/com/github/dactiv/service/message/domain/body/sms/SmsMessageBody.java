package com.github.dactiv.service.message.domain.body.sms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.service.message.domain.entity.BasicMessageEntity;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 短信 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsMessageBody extends BasicMessageEntity implements TypeUserDetails<Integer> {

    @Serial
    private static final long serialVersionUID = -6678810630364920364L;

    /**
     * 收件方集合
     */
    @NotEmpty
    private List<String> phoneNumbers = new LinkedList<>();

    /**
     * 发送用户 id
     */
    private Integer userId;

    /**
     * 发送用户名
     */
    private String username;

    /**
     * 发送用户类型
     */
    private String userType;
}
