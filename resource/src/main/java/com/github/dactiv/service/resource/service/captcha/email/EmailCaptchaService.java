package com.github.dactiv.service.resource.service.captcha.email;

import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.service.commons.service.domain.meta.TypeIdNameMeta;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import com.github.dactiv.service.resource.config.capthca.EmailCaptchaConfig;
import com.github.dactiv.service.resource.domain.body.captcha.EmailRequestBody;
import com.github.dactiv.service.resource.domain.meta.DataDictionaryMeta;
import com.github.dactiv.service.resource.service.captcha.AbstractMessageCaptchaService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 邮件验证码服务
 *
 * @author maurice
 */
@Component
@RequiredArgsConstructor
public class EmailCaptchaService extends AbstractMessageCaptchaService<EmailRequestBody> {

    public static final String DEFAULT_EMAIL_PARAM_NAME = "emailParamName";

    private final EmailCaptchaConfig emailCaptchaConfig;

    @Override
    protected String getReceivingTargetParamName() {
        return emailCaptchaConfig.getEmailParamName();
    }

    @Override
    protected Map<String, Object> createSendMessageParam(EmailRequestBody entity, DataDictionaryMeta entry, String captcha) {

        Map<String, Object> param = new LinkedHashMap<>();
        String title = entry.getName();
        if (title.contains(emailCaptchaConfig.getTitleSeparator())) {
            title = StringUtils.substringAfter(title, emailCaptchaConfig.getTitleSeparator());
        }

        // 通过短息实体生成短信信息
        String content = MessageFormat.format(
                entry.getValue().toString(),
                title,
                captcha,
                emailCaptchaConfig.getCaptchaExpireTime().toMinutes()
        );

        // 构造参数，提交给消息服务发送信息
        param.put(MessageServiceFeignClient.DEFAULT_TITLE_KEY, entry.getName());
        param.put(MessageServiceFeignClient.DEFAULT_CONTENT_KEY, content);
        param.put(MessageServiceFeignClient.Constants.Email.TO_EMAILS_FIELD, Collections.singletonList(entity.getEmail()));
        param.put(TypeIdNameMeta.TYPE_FIELD_NAME, MessageTypeEnum.SYSTEM.toString());

        param.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, MessageServiceFeignClient.DEFAULT_EMAIL_TYPE_VALUE);

        return param;
    }

    @Override
    protected String getInterceptorType() {
        return emailCaptchaConfig.getInterceptorType();
    }

    @Override
    protected String generateCaptcha() {
        return RandomStringUtils.randomNumeric(emailCaptchaConfig.getRandomNumericCount());
    }


    @Override
    protected TimeProperties getCaptchaExpireTime() {
        return emailCaptchaConfig.getCaptchaExpireTime();
    }

    @Override
    public String getCaptchaParamName() {
        return emailCaptchaConfig.getCaptchaParamName();
    }

    @Override
    public String getType() {
        return MessageServiceFeignClient.DEFAULT_EMAIL_TYPE_VALUE;
    }


    @Override
    protected Map<String, Object> createGenerateArgs() {
        Map<String, Object> generate = super.createGenerateArgs();

        generate.put(DEFAULT_EMAIL_PARAM_NAME, emailCaptchaConfig.getEmailParamName());
        generate.put(DEFAULT_TYPE_PARAM_NAME, emailCaptchaConfig.getTypeParamName());

        return generate;
    }
}
