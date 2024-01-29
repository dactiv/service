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
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
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

    public static final String DEFAULT_QUEUE_NAME = "dactiv.service.authentication.valid.info";

    private final AuthenticationInfoService authenticationInfoService;

    private final AbnormalAreaConfig abnormalAreaConfig;

    private final GatherServiceFeignClient gatherServiceFeignClient;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.AUTHENTICATION_RABBIT_MQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void validAuthenticationInfo(@Payload AuthenticationInfoEntity info,
                                        Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        if (Objects.nonNull(info.getIpRegionMeta()) && Objects.nonNull(info.getIpRegionMeta().getLocation())) {
            AddressRegionMeta addressRegionMeta = gatherServiceFeignClient.getMapRegion(
                    info.getIpRegionMeta().getLocation(),
                    abnormalAreaConfig.getLocationMapType()
            );
            info.getIpRegionMeta().setRegionMeta(addressRegionMeta);
        }

        authenticationInfoService.validAuthenticationInfo(info);

        channel.basicAck(tag, false);

    }

    public static void sendMessage(AmqpTemplate amqpTemplate, AuthenticationInfoEntity info) {
        amqpTemplate.convertAndSend(
                SystemConstants.AUTHENTICATION_RABBIT_MQ_EXCHANGE,
                DEFAULT_QUEUE_NAME,
                info,
                message -> {

                    if (Objects.isNull(info.getId())) {
                        return message;
                    }

                    String id = DEFAULT_QUEUE_NAME + Casts.DOT + info.getId();
                    message.getMessageProperties().setMessageId(id);
                    message.getMessageProperties().setCorrelationId(info.getId());

                    return message;
                }
        );
    }
}
