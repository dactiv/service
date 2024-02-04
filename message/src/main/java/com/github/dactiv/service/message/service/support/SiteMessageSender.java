package com.github.dactiv.service.message.service.support;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.message.config.SiteConfig;
import com.github.dactiv.service.message.domain.AttachmentMessage;
import com.github.dactiv.service.message.domain.body.site.SiteMessageBody;
import com.github.dactiv.service.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.service.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.service.message.service.SiteMessageService;
import com.github.dactiv.service.message.service.basic.BatchMessageSender;
import com.github.dactiv.service.message.service.support.site.SiteMessageChannelSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 站内信消息发送者
 *
 * @author maurice
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class SiteMessageSender extends BatchMessageSender<SiteMessageBody, SiteMessageEntity> {

    public static final String DEFAULT_QUEUE_NAME = "site_message_send";

    public static final String BATCH_UPDATE_CONCURRENT_KEY = "dactiv:service:message:site:batch-update:";

    /**
     * 默认的消息类型
     */
    public static final String DEFAULT_TYPE = "site";

    @Lazy
    @Getter
    private final SiteMessageService siteMessageService;

    private final List<SiteMessageChannelSender> siteMessageChannelSenderList;

    private final AmqpTemplate amqpTemplate;

    private final ConcurrentInterceptor concurrentInterceptor;

    private final SiteConfig config;

    @Override
    protected int getMaxRetryCount() {
        return config.getMaxRetryCount();
    }

    /**
     * 发送站内信
     *
     * @param id 站内信实体 id
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
    public RestResult<Object> doSend(List<SiteMessageEntity> content) {
        return super.doSend(content);
    }

    /**
     * 发送站内信
     *
     * @param id 站内信实体 id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SiteMessageEntity doSendMessage(Integer id) {

        SiteMessageEntity entity = siteMessageService.get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        if (ExecuteStatus.Success.equals(entity.getExecuteStatus())) {
            return entity;
        }

        List<SiteMessageChannelSender> siteMessageChannelSenders = getSiteMessageChannelSender(config.getChannel());

        entity.setChannel(config.getChannel());

        try {
            Map<String, RestResult<Map<String, Object>>> restResults = new LinkedHashMap<>();
            for (SiteMessageChannelSender sender : siteMessageChannelSenders) {
                RestResult<Map<String, Object>> result = sender.sendSiteMessage(entity);
                restResults.put(sender.getType(), result);
            }

            if (restResults.values().stream().allMatch(r -> config.getSuccessStatus().contains(r.getStatus()))) {
                ExecuteStatus.success(entity);
            } else {
                List<String> messages = restResults
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().getStatus() != HttpStatus.OK.value())
                        .map(e -> e.getKey() + CacheProperties.DEFAULT_SEPARATOR + e.getValue().getMessage())
                        .collect(Collectors.toList());
                ExecuteStatus.failure(entity, StringUtils.join(messages, Casts.COMMA));
            }

        } catch (Exception e) {
            if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, e.getMessage());
            } else {
                ExecuteStatus.retry(entity, e.getMessage());
                throw new SystemException(e);
            }
        } finally {
            siteMessageService.save(entity);
        }

        if (Objects.nonNull(entity.getBatchId())) {
            concurrentInterceptor.invoke(BATCH_UPDATE_CONCURRENT_KEY + entity.getId(), () -> updateBatchMessage(entity));
        }

        return entity;
    }

    /**
     * 获取站内信消息渠道发送者
     *
     * @param channel 渠道类型
     * @return 站内信消息渠道发送者
     */
    private List<SiteMessageChannelSender> getSiteMessageChannelSender(List<String> channel) {
        return siteMessageChannelSenderList
                .stream()
                .filter(s -> channel.contains(s.getType()))
                .collect(Collectors.toList());
    }

    /**
     * 通过站内信消息 body 构造站内信消息并保存信息
     *
     * @param body 站内信消息 body
     * @return 邮件消息流
     */
    private Stream<SiteMessageEntity> createSiteMessage(SiteMessageBody body) {

        List<SiteMessageEntity> result = new LinkedList<>();

        for (BasicUserDetails<Integer> meta : body.getToUsers()) {

            SiteMessageEntity entity = ofEntity(body);

            entity.setUserId(meta.getUserId());
            entity.setUsername(meta.getUsername());
            entity.setUserType(meta.getUserType());

            result.add(entity);
        }

        return result.stream();
    }

    /**
     * 创建站内信消息实体
     *
     * @param body 站内信消息 body
     * @return 站内信消息实体
     */
    private SiteMessageEntity ofEntity(SiteMessageBody body) {
        SiteMessageEntity entity = Casts.of(body, SiteMessageEntity.class, AttachmentMessage.ATTACHMENT_LIST_FIELD_NAME);

        if (CollectionUtils.isNotEmpty(body.getAttachmentList())) {
            entity.setAttachmentList(body.getAttachmentList());
        }

        return entity;
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SiteMessageEntity> preSend(List<SiteMessageEntity> content) {
        siteMessageService.save(content);
        return super.preSend(content);
    }

    @Override
    protected RestResult<Object> send(List<SiteMessageEntity> entities) {

        entities.forEach(e -> amqpTemplate.convertAndSend(SystemConstants.MESSAGE_RABBIT_EXCHANGE, DEFAULT_MESSAGE_COUNT_KEY, e.getId()));

        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条站内信消息完成",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<SiteMessageEntity> getBatchMessageBodyContent(List<SiteMessageBody> result) {
        return result.stream().flatMap(this::createSiteMessage).collect(Collectors.toList());
    }
}
