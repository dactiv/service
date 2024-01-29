package com.github.dactiv.service.authentication.consumer;

import com.github.dactiv.service.authentication.domain.entity.MerchantClientEntity;
import com.github.dactiv.service.authentication.service.MerchantClientService;
import com.github.dactiv.service.commons.service.SystemConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

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

    /**
     * 数据变更队列名称
     */
    public static final String DEFAULT_DATA_CHANGE_QUEUE_NAME = "merchant_data_change";

    /**
     * 数据删除队列名称
     */
    public static final String DEFAULT_DATA_DELETE_QUEUE_NAME = "merchant_data_delete";

    private final MerchantClientService merchantClientService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_DATA_CHANGE_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.AUTHENTICATION_RABBIT_MQ_EXCHANGE),
                    key = DEFAULT_DATA_CHANGE_QUEUE_NAME
            )
    )
    public void onChange(Map<String, Object> message) {
        MerchantClientEntity entity = MerchantClientEntity.ofFlatMessageData(message);
        merchantClientService.createOrUpdateMerchantClient(entity);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_DATA_DELETE_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.AUTHENTICATION_RABBIT_MQ_EXCHANGE),
                    key = DEFAULT_DATA_DELETE_QUEUE_NAME
            )
    )
    public void onDelete(Integer id) {
        merchantClientService.deleteByMerchantId(List.of(id));
    }
}
