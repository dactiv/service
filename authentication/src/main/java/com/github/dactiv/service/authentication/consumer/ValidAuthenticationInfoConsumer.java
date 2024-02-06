package com.github.dactiv.service.authentication.consumer;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.service.authentication.config.AbnormalAreaConfig;
import com.github.dactiv.service.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.service.authentication.service.AuthenticationInfoService;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.domain.meta.AddressRegionMeta;
import com.github.dactiv.service.commons.service.feign.GatherServiceFeignClient;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * 验证认证信息消费者
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidAuthenticationInfoConsumer {

    public static final String DEFAULT_QUEUE_NAME = SystemConstants.AUTHENTICATION_RABBIT_EXCHANGE + Casts.UNDERSCORE + "valid_info";

    private final AuthenticationInfoService authenticationInfoService;

    private final AbnormalAreaConfig abnormalAreaConfig;

    private final GatherServiceFeignClient gatherServiceFeignClient;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.AUTHENTICATION_RABBIT_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void validAuthenticationInfo(String data,
                                        Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {

            AuthenticationInfoEntity info = Casts.readValue(data, AuthenticationInfoEntity.class);

            if (Objects.nonNull(info.getIpRegionMeta()) && Objects.nonNull(info.getIpRegionMeta().getLocation())) {
                AddressRegionMeta addressRegionMeta = gatherServiceFeignClient.getMapRegion(
                        info.getIpRegionMeta().getLocation(),
                        abnormalAreaConfig.getLocationMapType()
                );
                info.getIpRegionMeta().setRegionMeta(addressRegionMeta);
            }

            authenticationInfoService.validAuthenticationInfo(info);

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.warn("校验用户认证信息出现错误", e);
            channel.basicNack(tag, false, false);
        }
    }
}
