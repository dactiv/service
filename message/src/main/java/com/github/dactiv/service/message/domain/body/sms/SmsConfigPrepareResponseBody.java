package com.github.dactiv.service.message.domain.body.sms;

import com.github.dactiv.service.commons.service.domain.meta.ConstructionCaptchaMeta;
import com.github.dactiv.service.message.domain.meta.sms.SmsConfigPrepareMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 短信配置预处理响应体
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsConfigPrepareResponseBody extends SmsConfigPrepareMeta {

    @Serial
    private static final long serialVersionUID = -3063713077733696745L;

    private ConstructionCaptchaMeta captchaMeta;
}
