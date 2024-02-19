package com.github.dactiv.service.commons.service.domain.meta;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.captcha.CaptchaProperties;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import com.github.dactiv.service.commons.service.feign.ResourceServiceFeignClient;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 构造验证码元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class ConstructionCaptchaMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -2135082237130733550L;

    @NonNull
    private String type;

    @NonNull
    private Map<String, Object> args;

    public static Map<String, Object> toSmsParam(Map<String, Object> buildTokenMap, String phoneNumber) {
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> generate = putAndGetArgs(buildTokenMap, result);
        String phoneNumberParamName = generate.getOrDefault(ResourceServiceFeignClient.PHONE_NUMBER_PARAM_NAME, StringUtils.EMPTY).toString();
        result.put(phoneNumberParamName, phoneNumber);

        return result;
    }

    public static Map<String, Object> toEmailParam(Map<String, Object> buildTokenMap,
                                                           String email,
                                                           String messageType) {
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> generate = putAndGetArgs(buildTokenMap, result);
        String emailParamName = generate.getOrDefault(ResourceServiceFeignClient.EMAIL_PARAM_NAME, StringUtils.EMPTY).toString();
        result.put(emailParamName, email);
        result.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, messageType);
        return result;
    }

    public static RestResult<Object> generateCaptcha(ResourceServiceFeignClient resourceServiceFeignClient, Map<String, Object> generateParam) {
        Object object = resourceServiceFeignClient.generateCaptcha(generateParam);
        RestResult<Object> generateResult = Casts.convertValue(object, new TypeReference<>() {
        });

        Assert.isTrue(generateResult.getStatus() == HttpStatus.OK.value(), generateResult.getMessage());

        return generateResult;
    }

    private static Map<String, Object> putAndGetArgs(Map<String, Object> buildTokenMap, Map<String, Object> reference) {

        reference.put(CaptchaProperties.DEFAULT_CAPTCHA_TYPE_PARAM_NAME, buildTokenMap.get(TypeIdNameMeta.TYPE_FIELD_NAME));

        String tokenParamName = buildTokenMap.getOrDefault(SystemConstants.CAPTCHA_TOKEN_PARAM_NAME, StringUtils.EMPTY).toString();
        CacheProperties properties = Casts.convertValue(buildTokenMap.get(SystemConstants.TOKEN_FIELD_NAME), CacheProperties.class);
        reference.put(tokenParamName, properties.getName());

        Map<String, Object> args = Casts.convertValue(buildTokenMap.get(ResourceServiceFeignClient.ARGS_FIELD_NAME), Casts.MAP_TYPE_REFERENCE);
        return Casts.convertValue(args.get(ResourceServiceFeignClient.ARGS_GENERATE_FIELD_NAME), Casts.MAP_TYPE_REFERENCE);
    }
}
