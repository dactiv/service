package com.github.dactiv.service.authentication.security.handler;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.jackson.serializer.DesensitizeSerializer;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessResponse;
import com.github.dactiv.framework.spring.security.authentication.service.DefaultUserDetailsService;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.service.authentication.consumer.ValidAuthenticationInfoConsumer;
import com.github.dactiv.service.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.domain.meta.IpRegionMeta;
import com.github.dactiv.service.commons.service.domain.meta.LocationIpRegionMeta;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证成功的一些数据处理实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationSuccessResponse implements JsonAuthenticationSuccessResponse {

    private final AuthenticationFailureResponse jsonAuthenticationFailureHandler;

    private final AmqpTemplate amqpTemplate;

    @Override
    public void setting(RestResult<Object> result, HttpServletRequest request) {

        Object details = result.getData();
        if (!SecurityUserDetails.class.isAssignableFrom(details.getClass())) {
            return;
        }

        SecurityUserDetails userDetails = Casts.cast(details);

        if (DefaultUserDetailsService.DEFAULT_TYPES.equals(userDetails.getType())) {
            return;
        }

        jsonAuthenticationFailureHandler.deleteAllowableFailureNumber(request);

        UserAgent device = DeviceUtils.getRequiredCurrentDevice(request);
        String ip = SpringMvcUtils.getIpAddress(request);

        AuthenticationInfoEntity info = new AuthenticationInfoEntity();
        info.setDevice(device.toMap());
        info.setMeta(new LinkedHashMap<>(userDetails.getMeta()));
        info.setIpRegionMeta(Casts.convertValue(IpRegionMeta.of(ip), LocationIpRegionMeta.class));
        info.setUserDetails(SecurityUserDetailsConstants.toBasicUserDetails(userDetails));

        amqpTemplate.convertAndSend(
                SystemConstants.AUTHENTICATION_RABBIT_EXCHANGE,
                ValidAuthenticationInfoConsumer.DEFAULT_QUEUE_NAME,
                Casts.writeValueAsString(info)
        );

        postSecurityUserDetails(userDetails);
    }

    public void postSecurityUserDetails(SecurityUserDetails userDetails) {
        if (MapUtils.isEmpty(userDetails.getMeta())) {
            return;
        }

        Map<String, Object> meta = new LinkedHashMap<>(userDetails.getMeta());

        List<String> desensitizeFields = jsonAuthenticationFailureHandler.getApplicationConfig().getDesensitizePrincipalProperties();
        for (String desensitizeField : desensitizeFields) {
            String value = meta.getOrDefault(desensitizeField, StringUtils.EMPTY).toString();
            if (StringUtils.isNotEmpty(value)) {
                meta.put(desensitizeField, DesensitizeSerializer.desensitize(value));
            }
        }

        userDetails.setMeta(meta);
    }

}
