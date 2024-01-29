package com.github.dactiv.service.resource.service.captcha.sms;

import com.github.dactiv.framework.captcha.InterceptToken;
import com.github.dactiv.framework.captcha.ReceivingTargetSimpleCaptcha;
import com.github.dactiv.framework.captcha.SimpleCaptcha;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.service.commons.service.domain.meta.TypeIdNameMeta;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import com.github.dactiv.service.commons.service.feign.ResourceServiceFeignClient;
import com.github.dactiv.service.resource.config.capthca.SmsCaptchaConfig;
import com.github.dactiv.service.resource.domain.body.captcha.SmsRequestBody;
import com.github.dactiv.service.resource.domain.meta.DataDictionaryMeta;
import com.github.dactiv.service.resource.service.captcha.AbstractMessageCaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信验证码服务
 *
 * @author maurice
 */
@Component
@RequiredArgsConstructor
public class SmsCaptchaService extends AbstractMessageCaptchaService<SmsRequestBody> {

    private final SmsCaptchaConfig smsCaptchaConfig;

    @Override
    protected Map<String, Object> createSendMessageParam(SmsRequestBody entity, DataDictionaryMeta entry, String captcha) {

        Map<String, Object> param = new LinkedHashMap<>();

        // 通过短息实体生成短信信息
        String content = MessageFormat.format(
                entry.getValue().toString(),
                entry.getName(),
                captcha,
                smsCaptchaConfig.getCaptchaExpireTime().toMinutes()
        );

        // 构造参数，提交给消息服务发送信息
        param.put(MessageServiceFeignClient.DEFAULT_CONTENT_KEY, content);
        param.put(MessageServiceFeignClient.Constants.Sms.PHONE_NUMBERS_FIELD, Collections.singletonList(entity.getPhoneNumber()));
        param.put(TypeIdNameMeta.TYPE_FIELD_NAME, MessageTypeEnum.SYSTEM.toString());

        param.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, MessageServiceFeignClient.DEFAULT_SMS_TYPE_VALUE);

        return param;
    }

    @Override
    protected String getInterceptorType() {
        return smsCaptchaConfig.getInterceptorType();
    }

    @Override
    protected SimpleCaptcha createMatchCaptcha(String value, HttpServletRequest request, InterceptToken buildToken, SmsRequestBody requestBody) {
        SimpleCaptcha captcha = super.createMatchCaptcha(value, request, buildToken, requestBody);
        ReceivingTargetSimpleCaptcha targetSimpleCaptcha = Casts.of(captcha, ReceivingTargetSimpleCaptcha.class);
        targetSimpleCaptcha.setTarget(requestBody.getPhoneNumber());

        return targetSimpleCaptcha;
    }

    @Override
    protected String generateCaptcha() {
        return RandomStringUtils.randomNumeric(smsCaptchaConfig.getRandomNumericCount());
    }

    @Override
    protected TimeProperties getCaptchaExpireTime() {
        return smsCaptchaConfig.getCaptchaExpireTime();
    }

    @Override
    protected TimeProperties getRetryTime() {
        return smsCaptchaConfig.getRetryTime();
    }

    @Override
    public String getType() {
        return MessageServiceFeignClient.DEFAULT_SMS_TYPE_VALUE;
    }

    @Override
    public String getReceivingTargetParamName() {
        return smsCaptchaConfig.getPhoneNumberParamName();
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {
        Map<String, Object> result = super.createGenerateArgs();

        result.put(ResourceServiceFeignClient.PHONE_NUMBER_PARAM_NAME, getReceivingTargetParamName());
        result.put(DEFAULT_TYPE_PARAM_NAME, smsCaptchaConfig.getTypeParamName());
        return result;
    }

    @Override
    public String getCaptchaParamName() {
        return smsCaptchaConfig.getCaptchaParamName();
    }

}
