package com.github.dactiv.service.message.service.support.sms.tencent;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.service.message.config.ApplicationConfig;
import com.github.dactiv.service.message.config.SmsConfig;
import com.github.dactiv.service.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.service.message.domain.meta.sms.SmsBalanceMeta;
import com.github.dactiv.service.message.service.support.sms.SmsChannelSender;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 腾讯短信渠道发送者实现
 *
 * @author maurice.chen
 */
@Component
public class TencentSmsChannelSender implements SmsChannelSender {

    public static final String DEFAULT_TYPE = "tencent";

    public static final String REQUEST_BODY_NAME = "requestBody";

    public static final String RESPONSE_BODY_NAME = "responseBody";

    private final ApplicationConfig applicationConfig;

    private final SmsConfig smsConfig;

    public TencentSmsChannelSender(ApplicationConfig applicationConfig, SmsConfig smsConfig) {
        this.applicationConfig = applicationConfig;
        this.smsConfig = smsConfig;
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public String getName() {
        return "腾讯短信渠道";
    }

    @Override
    public RestResult<Map<String, Object>> sendSms(SmsMessageEntity entity) {
        try {
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
            Credential cred = new Credential(
                    applicationConfig.getTencentCloud().getSecretId(),
                    applicationConfig.getTencentCloud().getSecretKey()
            );
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(smsConfig.getTencent().getEndpoint());
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SmsClient client = new SmsClient(cred, smsConfig.getTencent().getRegion(), clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();

            req.setPhoneNumberSet(new String[]{entity.getPhoneNumber()});
            req.setSmsSdkAppId(smsConfig.getTencent().getSdkAppId());
            req.setSignName(smsConfig.getTencent().getSign());
            req.setTemplateId(smsConfig.getTencent().getTemplateId());
            req.setTemplateParamSet(new String[]{entity.getContent()});

            entity.getMeta().put(REQUEST_BODY_NAME, Casts.convertValue(req, Casts.MAP_TYPE_REFERENCE));

            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            Map<String, Object> responseBody = Casts.convertValue(client.SendSms(req), Casts.MAP_TYPE_REFERENCE);
            entity.getMeta().put(RESPONSE_BODY_NAME, responseBody);
            return RestResult.ofSuccess(responseBody);
        } catch (TencentCloudSDKException e) {
            throw new ErrorCodeException(e.getMessage(), e.getErrorCode());
        }
    }

    @Override
    public SmsBalanceMeta getBalance() {
        return null;
    }
}
