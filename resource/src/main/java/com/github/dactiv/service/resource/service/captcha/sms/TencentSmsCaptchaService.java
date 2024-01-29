package com.github.dactiv.service.resource.service.captcha.sms;

import com.github.dactiv.framework.captcha.AbstractRedissonStorageCaptchaService;
import com.github.dactiv.framework.captcha.GenerateCaptchaResult;
import com.github.dactiv.framework.captcha.InterceptToken;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.service.commons.service.domain.meta.TypeIdNameMeta;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import com.github.dactiv.service.commons.service.feign.ResourceServiceFeignClient;
import com.github.dactiv.service.resource.config.capthca.SmsCaptchaConfig;
import com.github.dactiv.service.resource.domain.body.captcha.TencentSmsRequestBody;
import com.github.dactiv.service.resource.service.captcha.AbstractMessageCaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 腾讯验证码服务
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TencentSmsCaptchaService extends AbstractRedissonStorageCaptchaService<TencentSmsRequestBody> {

    public static final String DEFAULT_TYPE = "tencentSms";

    private final SmsCaptchaConfig smsCaptchaConfig;

    private final MessageServiceFeignClient messageServiceFeignClient;

    @Override
    protected GenerateCaptchaResult doGenerateCaptcha(InterceptToken buildToken,
                                                      TencentSmsRequestBody requestBody,
                                                      HttpServletRequest request) {

        String captcha = RandomStringUtils.randomNumeric(smsCaptchaConfig.getRandomNumericCount());

        Map<String, Object> param = new LinkedHashMap<>();
        param.put(MessageServiceFeignClient.DEFAULT_CONTENT_KEY, captcha);
        param.put(MessageServiceFeignClient.Constants.Sms.PHONE_NUMBERS_FIELD, Collections.singletonList(requestBody.getPhoneNumber()));
        param.put(TypeIdNameMeta.TYPE_FIELD_NAME, MessageTypeEnum.SYSTEM.toString());

        param.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, MessageServiceFeignClient.DEFAULT_SMS_TYPE_VALUE);

        RestResult<Object> result = messageServiceFeignClient.send(param);
        // 如果发送成记录短信验证码到 redis 中给校验备用。
        Assert.isTrue(result.isSuccess(), result.getMessage());
        RestResult<Map<String, Object>> codeLengthResult = AbstractMessageCaptchaService.createCodeLengthResult(GenerateCaptchaResult.of(result, captcha), getRetryTime());
        return GenerateCaptchaResult.of(codeLengthResult, captcha);

    }

    @Override
    protected String getInterceptorType() {
        return smsCaptchaConfig.getInterceptorType();
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {
        Map<String, Object> result = super.createGenerateArgs();
        result.put(ResourceServiceFeignClient.PHONE_NUMBER_PARAM_NAME, smsCaptchaConfig.getPhoneNumberParamName());
        return result;
    }

    @Override
    public String getCaptchaParamName() {
        return smsCaptchaConfig.getCaptchaParamName();
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
        return DEFAULT_TYPE;
    }
}
