package com.github.dactiv.service.message.service.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.minio.FilenameObject;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import com.github.dactiv.service.message.config.MailConfig;
import com.github.dactiv.service.message.domain.body.email.EmailMessageBody;
import com.github.dactiv.service.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.service.message.domain.entity.EmailMessageEntity;
import com.github.dactiv.service.message.service.EmailMessageService;
import com.github.dactiv.service.message.service.basic.BatchMessageSender;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 邮件消息发送者实现
 *
 * @author maurice
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class EmailMessageSender extends BatchMessageSender<EmailMessageBody, EmailMessageEntity> implements InitializingBean {

    /**
     * 数据变更队列名称
     */
    public static final String DEFAULT_QUEUE_NAME = SystemConstants.MESSAGE_RABBIT_EXCHANGE + "_email_send";

    public static final String BATCH_UPDATE_CONCURRENT_KEY = "dactiv:service:message:email:batch-update:";

    private final Map<String, JavaMailSenderImpl> mailSenderMap = new LinkedHashMap<>();

    private final AmqpTemplate amqpTemplate;

    @Lazy
    @Getter
    private final EmailMessageService emailMessageService;

    private final ConcurrentInterceptor concurrentInterceptor;

    private final MailConfig config;

    @Override
    protected RestResult<Object> send(List<EmailMessageEntity> entities) {
        entities.forEach(e -> amqpTemplate.convertAndSend(SystemConstants.MESSAGE_RABBIT_EXCHANGE, DEFAULT_QUEUE_NAME, e.getId()));
        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条邮件消息完成",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected int getMaxRetryCount() {
        return config.getMaxRetryCount();
    }

    @Override
    protected List<EmailMessageEntity> getBatchMessageBodyContent(List<EmailMessageBody> result) {
        return result.stream().flatMap(this::createEmailMessageEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Object> doSend(List<EmailMessageEntity> content) {
        return super.doSend(content);
    }

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

    /**
     * 发送邮件
     *
     * @param id 邮件实体 id
     */
    @Transactional(rollbackFor = Exception.class)
    public EmailMessageEntity doSendMessage(Integer id) {

        EmailMessageEntity entity = emailMessageService.get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        if (ExecuteStatus.Success.equals(entity.getExecuteStatus())) {
            return entity;
        }

        entity.setLastSendTime(new Date());

        JavaMailSenderImpl mailSender = mailSenderMap.get(entity.getType().toString().toLowerCase());

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(entity.getFromEmail(), config.getPersonal());
            helper.setTo(entity.getToEmail());
            helper.setSubject(entity.getTitle());
            helper.setText(entity.getContent(), true);

            if (CollectionUtils.isNotEmpty(entity.getAttachmentList())) {

                for (FilenameObject fileObject : entity.getAttachmentList()) {

                    InputStreamSource iss;

                    if (Objects.nonNull(entity.getBatchId()) && getAttachmentCache().containsKey(entity.getBatchId())) {
                        Map<String, byte[]> fileMap = getAttachmentCache().get(entity.getBatchId());
                        if (fileMap.containsKey(fileObject.getFilename())) {
                            byte[] bytes = fileMap.get(fileObject.getFilename());
                            iss = new ByteArrayResource(bytes);
                        } else {
                            byte[] file = getResourceServiceFeignClient().getAttachmentFile(fileObject.getBucketName(), fileObject.getObjectName());
                            iss = new ByteArrayResource(file);
                        }
                    } else {
                        byte[] file = getResourceServiceFeignClient().getAttachmentFile(fileObject.getBucketName(), fileObject.getObjectName());
                        iss = new ByteArrayResource(file);
                    }

                    helper.addAttachment(fileObject.getFilename(), iss);
                }

            }

            mailSender.send(mimeMessage);

            ExecuteStatus.success(entity);
        } catch (Exception e) {
            if (!entity.isRetry()) {
                ExecuteStatus.failure(entity, e.getMessage());
            } else {
                ExecuteStatus.retry(entity, e.getMessage());
                throw new SystemException(e);
            }
        } finally {
            emailMessageService.save(entity);
        }

        if (Objects.nonNull(entity.getBatchId())) {
            concurrentInterceptor.invoke(BATCH_UPDATE_CONCURRENT_KEY + entity.getId(), () -> updateBatchMessage(entity));
        }

        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<EmailMessageEntity> preSend(List<EmailMessageEntity> content) {
        emailMessageService.save(content);
        return super.preSend(content);
    }

    /**
     * 通过邮件消息 body 构造邮件消息并保存信息
     *
     * @param body 邮件消息 body
     * @return 邮件消息流
     */
    private Stream<EmailMessageEntity> createEmailMessageEntity(EmailMessageBody body) {

        List<EmailMessageEntity> result = new LinkedList<>();

        for (String toEmail : body.getToEmails()) {

            EmailMessageEntity entity = ofEntity(body);
            entity.setToEmail(toEmail);

            result.add(entity);
        }

        return result.stream();
    }

    /**
     * 创建邮件消息实体
     *
     * @param body 邮件消息 body
     * @return 邮件消息实体
     */
    private EmailMessageEntity ofEntity(EmailMessageBody body) {
        EmailMessageEntity entity = Casts.of(body, EmailMessageEntity.class, "attachmentList");

        JavaMailSenderImpl mailSender = Objects.requireNonNull(
                mailSenderMap.get(entity.getType().toString().toLowerCase()),
                "找不到类型为 [" + entity.getType() + "] 的邮件发送者"
        );

        Assert.isTrue(StringUtils.isNotEmpty(mailSender.getUsername()), "类型为 [" + entity.getType().toString().toLowerCase() + "] 的邮件发送者 username 为空");

        entity.setFromEmail(mailSender.getUsername());

        if (CollectionUtils.isNotEmpty(body.getAttachmentList())) {
            entity.setAttachmentList(body.getAttachmentList());
        }

        return entity;
    }

    @Override
    public String getMessageType() {
        return MessageServiceFeignClient.DEFAULT_EMAIL_TYPE_VALUE;
    }

    @Override
    public void afterPropertiesSet() {
        config.getAccounts().entrySet().forEach(this::generateMailSender);
    }

    /**
     * 生成邮件发送者
     *
     * @param entry 账户配置信息
     */
    private void generateMailSender(Map.Entry<String, MailProperties> entry) {

        MailProperties mailProperties = entry.getValue();

        JavaMailSenderImpl mailSender = mailSenderMap.computeIfAbsent(
                entry.getKey(),
                k -> new JavaMailSenderImpl()
        );

        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());

        if (MapUtils.isNotEmpty(mailProperties.getProperties())) {
            mailSender.getJavaMailProperties().putAll(mailProperties.getProperties());
        }

        if (MapUtils.isEmpty(mailSender.getJavaMailProperties()) && MapUtils.isNotEmpty(config.getProperties())) {
            mailSender.getJavaMailProperties().putAll(config.getProperties());
        }

        mailSender.setHost(StringUtils.defaultIfEmpty(mailProperties.getHost(), config.getHost()));
        mailSender.setPort(Objects.nonNull(mailProperties.getPort()) ? mailProperties.getPort() : config.getPort());
        mailSender.setProtocol(StringUtils.defaultIfEmpty(mailProperties.getProtocol(), config.getProtocol()));

        Charset encoding = Objects.nonNull(mailProperties.getDefaultEncoding()) ? mailProperties.getDefaultEncoding() : config.getDefaultEncoding();
        if (Objects.nonNull(encoding)) {
            mailSender.setDefaultEncoding(encoding.toString());
        }

        String jndiName = StringUtils.defaultIfEmpty(mailProperties.getJndiName(), config.getJndiName());

        if (StringUtils.isNotBlank(jndiName)) {
            try {
                Session session = JndiLocatorDelegate.createDefaultResourceRefLocator().lookup(jndiName, Session.class);
                mailSender.setSession(session);
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Unable to find Session in JNDI location %s", jndiName), e);
            }
        }

    }
}
