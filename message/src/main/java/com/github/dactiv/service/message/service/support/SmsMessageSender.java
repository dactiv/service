package com.github.dactiv.service.message.service.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.message.config.SmsConfig;
import com.github.dactiv.service.message.domain.body.sms.SmsMessageBody;
import com.github.dactiv.service.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.service.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.service.message.service.SmsMessageService;
import com.github.dactiv.service.message.service.basic.BatchMessageSender;
import com.github.dactiv.service.message.service.support.sms.SmsChannelSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 短信消息发送者实现
 *
 * @author maurice
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class SmsMessageSender extends BatchMessageSender<SmsMessageBody, SmsMessageEntity> {

    public static final String DEFAULT_QUEUE_NAME = SystemConstants.MESSAGE_RABBIT_EXCHANGE + "_sms_send";

    public static final String BATCH_UPDATE_CONCURRENT_KEY = "dactiv:service:message:sms:batch-update:";

    public static final String DEFAULT_TYPE = "sms";

    @Lazy
    @Getter
    private final SmsMessageService smsMessageService;

    @Getter
    private final List<SmsChannelSender> smsChannelSenderList;

    private final AmqpTemplate amqpTemplate;

    private final SmsConfig config;

    private final ConcurrentInterceptor concurrentInterceptor;

    @Override
    protected int getMaxRetryCount() {
        return config.getMaxRetryCount();
    }

    /**
     * 发送短信
     *
     * @param id 短信实体 id
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.MESSAGE_RABBIT_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(Integer id) {
        super.sendMessage(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Object> doSend(List<SmsMessageEntity> content) {
        return super.doSend(content);
    }

    /**
     * 发送短信
     *
     * @param id 短信实体 id
     * @return 短信消息实体
     */
    @Transactional(rollbackFor = Exception.class)
    public SmsMessageEntity doSendMessage(Integer id) {

        SmsMessageEntity entity = smsMessageService.get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        if (ExecuteStatus.Success.equals(entity.getExecuteStatus())) {
            return entity;
        }

        SmsChannelSender smsChannelSender = getSmsChannelSender(config.getChannel());

        entity.setChannel(smsChannelSender.getType());

        try {

            RestResult<Map<String, Object>> restResult = smsChannelSender.sendSms(entity);

            if (restResult.getStatus() == HttpStatus.OK.value()) {
                ExecuteStatus.success(entity);
            } else if (restResult.getStatus() == HttpStatus.PROCESSING.value() && log.isDebugEnabled()) {
                log.debug("ID 为 [" + entity.getId() + "] (" + entity.getPhoneNumber() + ":" + entity.getContent() + ") 的短信数据正在处理中.");
            } else if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, restResult.getMessage());
            } else {
                entity.setExecuteStatus(ExecuteStatus.Retrying);
            }
        } catch (Exception e) {

            if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, e.getMessage());
            } else {
                ExecuteStatus.retry(entity, e.getMessage());
                throw new SystemException(e);
            }

        } finally {
            smsMessageService.save(entity);
        }


        if (Objects.nonNull(entity.getBatchId())) {
            concurrentInterceptor.invoke(BATCH_UPDATE_CONCURRENT_KEY + entity.getId(), () -> updateBatchMessage(entity));
        }

        return entity;
    }

    /**
     * 获取发送短信的渠道发送者
     *
     * @param channel 渠道类型
     * @return 短信渠道发送者
     */
    private SmsChannelSender getSmsChannelSender(String channel) {
        return smsChannelSenderList
                .stream()
                .filter(s -> channel.equals(s.getType()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到渠道为[" + channel + "]的短信渠道支持"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SmsMessageEntity> preSend(List<SmsMessageEntity> content) {
        smsMessageService.save(content);
        return super.preSend(content);
    }

    @Override
    protected RestResult<Object> send(List<SmsMessageEntity> entities) {
        entities.forEach(e -> amqpTemplate.convertAndSend(SystemConstants.MESSAGE_RABBIT_EXCHANGE, DEFAULT_MESSAGE_COUNT_KEY, e.getId()));

        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条短信消息完成",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<SmsMessageEntity> getBatchMessageBodyContent(List<SmsMessageBody> result) {
        return result.stream().flatMap(this::createSmsMessageEntity).collect(Collectors.toList());
    }

    /**
     * 通过短信消息 body 构造短信消息并保存信息
     *
     * @param body 短信消息 body
     * @return 短信消息流
     */
    private Stream<SmsMessageEntity> createSmsMessageEntity(SmsMessageBody body) {
        body.setContent(StringUtils.deleteWhitespace(body.getContent()));
        List<SmsMessageEntity> result = new LinkedList<>();


        for (String phoneNumber : body.getPhoneNumbers()) {

            SmsMessageEntity entity = Casts.of(body, SmsMessageEntity.class);
            entity.setPhoneNumber(phoneNumber);

            result.add(entity);
        }

        return result.stream();
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }
}
