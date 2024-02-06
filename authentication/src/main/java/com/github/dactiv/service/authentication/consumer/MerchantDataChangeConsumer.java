package com.github.dactiv.service.authentication.consumer;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.service.authentication.domain.entity.MerchantClientEntity;
import com.github.dactiv.service.authentication.service.MerchantClientService;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 商户数据变更消费者
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantDataChangeConsumer {

    private final MerchantClientService merchantClientService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = SystemConstants.MERCHANT_DATA_CHANGE_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.RESOURCE_RABBIT_EXCHANGE, type = ExchangeTypes.FANOUT),
                    key = SystemConstants.MERCHANT_DATA_CHANGE_QUEUE_NAME
            )
    )
    public void onChange(String data,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        Map<String, Object> message = Casts.readValue(data, Casts.MAP_TYPE_REFERENCE);
        MerchantClientEntity entity = MerchantClientEntity.ofFlatMessageData(message);
        merchantClientService.createOrUpdateMerchantClient(entity);
        channel.basicAck(tag, false);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = SystemConstants.MERCHANT_DATA_DELETE_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.RESOURCE_RABBIT_EXCHANGE, type = ExchangeTypes.FANOUT),
                    key = SystemConstants.MERCHANT_DATA_DELETE_QUEUE_NAME
            )
    )
    public void onDelete(Integer id,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        merchantClientService.deleteByMerchantId(List.of(id));
        channel.basicAck(tag, false);
    }
}
