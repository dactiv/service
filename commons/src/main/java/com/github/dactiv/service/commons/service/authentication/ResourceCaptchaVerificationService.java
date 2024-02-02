package com.github.dactiv.service.commons.service.authentication;

import com.github.dactiv.framework.captcha.filter.CaptchaVerificationService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.service.commons.service.config.CommonsConfig;
import com.github.dactiv.service.commons.service.feign.ResourceServiceFeignClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * 资源服务验证码校验服务实现
 *
 * @author maurice.chen
 */
public class ResourceCaptchaVerificationService implements CaptchaVerificationService {

    private final CommonsConfig commonsConfig;

    private final ResourceServiceFeignClient resourceServiceFeignClient;

    public ResourceCaptchaVerificationService(ResourceServiceFeignClient resourceServiceFeignClient,
                                              CommonsConfig commonsConfig) {
        this.resourceServiceFeignClient = resourceServiceFeignClient;
        this.commonsConfig = commonsConfig;
    }

    @Override
    public List<String> getType() {
        return commonsConfig.getCaptchaVerificationType();
    }

    @Override
    public void verify(HttpServletRequest request) {
        Map<String, Object> param = Casts.castArrayValueMapToObjectValueMap(request.getParameterMap());
        RestResult<Object> result = resourceServiceFeignClient.verifyCaptcha(param);
        Assert.isTrue(result.getStatus() == HttpStatus.OK.value(), result.getMessage());
    }
}
