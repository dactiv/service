package com.github.dactiv.service.message.service.basic;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.minio.FilenameObject;
import com.github.dactiv.framework.commons.retry.Retryable;
import com.github.dactiv.service.commons.service.feign.ResourceServiceFeignClient;
import com.github.dactiv.service.message.domain.AttachmentMessage;
import com.github.dactiv.service.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.service.message.domain.entity.BatchMessageEntity;
import com.github.dactiv.service.message.enumerate.AttachmentTypeEnum;
import com.github.dactiv.service.message.service.BatchMessageService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;

import java.util.*;

/**
 * 批量消息发送的抽象实现，用于对需要创建 tb_batch_message 记录的消息做一个统一处理
 *
 * @param <T> 批量消息数据的泛型实体类型
 * @param <S> 请求的消息数据泛型实体类型
 */
@Getter
@Slf4j
public abstract class BatchMessageSender<T extends BasicMessageEntity, S extends BatchMessageEntity.Body> extends AbstractMessageSender<T> {

    public static final String DEFAULT_MESSAGE_COUNT_KEY = "count";

    public static final String DEFAULT_BATCH_MESSAGE_ID_KEY = "batchId";

    private BatchMessageService batchMessageService;

    /**
     * 文件管理服务
     */
    private ResourceServiceFeignClient resourceServiceFeignClient;

    /**
     * 线程池，用于批量发送消息时候异步使用。
     */
    private AsyncTaskExecutor asyncTaskExecutor;

    private final Class<S> sendEntityClass;

    /**
     * 批量消息对应的附件缓存 map
     */
    private final Map<Integer, Map<String, byte[]>> attachmentCache = new LinkedHashMap<>();

    public BatchMessageSender() {
        this.sendEntityClass = ReflectionUtils.getGenericClass(this, 1);
    }

    @Override
    public RestResult<Object> sendMessage(List<T> result) {

        List<S> content = getBatchMessageBodyContent(result);

        Objects.requireNonNull(content, "批量消息 body 内容不能为空");

        RestResult<Object> restResult = RestResult.ofException(
                String.valueOf(HttpStatus.NO_CONTENT.value()),
                new SystemException("未知执行结果")
        );

        content
                .stream()
                .filter(c -> Retryable.class.isAssignableFrom(c.getClass()))
                .map(c -> Casts.cast(c, Retryable.class))
                .forEach(c -> c.setMaxRetryCount(getMaxRetryCount()));

        if (content.size() > 1) {

            BatchMessageEntity batchMessage = new BatchMessageEntity();

            batchMessage.setCount(content.size());

            AttachmentTypeEnum attachmentType = AttachmentTypeEnum.valueOf(entityClass);
            batchMessage.setType(attachmentType);

            batchMessageService.save(batchMessage);

            content.forEach(r -> r.setBatchId(batchMessage.getId()));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put(DEFAULT_BATCH_MESSAGE_ID_KEY, batchMessage.getId());
            data.put(DEFAULT_MESSAGE_COUNT_KEY, content.size());

            asyncTaskExecutor.execute(() -> this.asyncTaskSend(batchMessage, result, content));

            restResult = RestResult.ofSuccess(
                    "发送" + content.size() + "条 [" + getMessageType() + "] 消息成功",
                    data
            );
        } else {
            RestResult<Object> sendResult = doSend(content);
            if (Objects.nonNull(sendResult)) {
                restResult = sendResult;
            }
        }

        return restResult;
    }

    private void asyncTaskSend(BatchMessageEntity batchMessage, List<T> result, List<S> content) {
        batchMessageCreated(batchMessage, result, content);
        doSend(content);
    }

    public RestResult<Object> doSend(List<S> content) {
        List<S> newData = preSend(content);

        if (CollectionUtils.isNotEmpty(newData)) {
            return send(newData);
        }

        return null;
    }

    /**
     * 发送消息前的处理
     *
     * @param content 批量消息内容
     * @return true 继续发送，否则 false
     */
    public List<S> preSend(List<S> content) {
        return content;
    }

    /**
     * 发送批量消息
     *
     * @param result 批量消息内容
     * @return rest 结果集
     */
    protected abstract RestResult<Object> send(List<S> result);

    /**
     * 获取批量消息数据内容
     *
     * @param result 消息的请求数据泛型实体集合
     * @return 批量消息数据的泛型实体集合
     */
    protected abstract List<S> getBatchMessageBodyContent(List<T> result);

    /**
     * 更新批量消息
     *
     * @param body 批量消息接口实现类
     */
    public void updateBatchMessage(BatchMessageEntity.Body body) {

        BatchMessageEntity batchMessage = batchMessageService.get(body.getBatchId());

        if (ExecuteStatus.Success.equals(body.getExecuteStatus())) {
            batchMessage.setSuccessNumber(batchMessage.getSuccessNumber() - 1);
        } else if (ExecuteStatus.Failure.equals(body.getExecuteStatus())) {
            batchMessage.setFailNumber(batchMessage.getFailNumber() + 1);
        }

        if (batchMessage.getCount().equals(batchMessage.getSuccessNumber() + batchMessage.getFailNumber())) {
            batchMessage.setExecuteStatus(ExecuteStatus.Success);
            batchMessage.setCompleteTime(new Date());

            onBatchMessageComplete(batchMessage);
        }

        batchMessageService.save(batchMessage);

    }

    /**
     * 当批量信息创建完成后，触发此方法
     *
     * @param batchMessage 批量信息实体
     * @param bodyResult   request 传过来的 body 参数集合
     */
    protected void batchMessageCreated(BatchMessageEntity batchMessage, List<T> bodyResult, List<S> content) {
        Map<String, byte[]> map = attachmentCache.computeIfAbsent(batchMessage.getId(), k -> new LinkedHashMap<>());

        List<FilenameObject> attachments = bodyResult
                .stream()
                .filter(t -> AttachmentMessage.class.isAssignableFrom(t.getClass()))
                .map(t -> Casts.cast(t, AttachmentMessage.class))
                .flatMap(t -> t.getAttachmentList().stream())
                .filter(a -> !map.containsKey(a.getFilename()))
                .toList();

        for (FilenameObject filenameObject : attachments) {
            try {
                byte[] file = resourceServiceFeignClient.getAttachmentFile(filenameObject.getBucketName(), filenameObject.getObjectName());
                map.put(filenameObject.getFilename(), file);
            } catch (Exception e) {
                log.error("读取 [" + filenameObject.getFilename() + "] 附件信息出现", e);
            }
        }
    }

    /**
     * 当批量信息发送完成时，触发此方法。
     *
     * @param batchMessage 批量信息实体
     */
    protected void onBatchMessageComplete(BatchMessageEntity batchMessage) {
        attachmentCache.remove(batchMessage.getId());
    }

    protected void sendMessage(Integer id) {
        S entity = doSendMessage(id);

        if (Objects.isNull(entity)) {
            log.warn("通过实现类:" + this.getClass().getSimpleName() + " 找不到 ID 为: " + id + " 的数据内容");
            return;
        }

        if (entity instanceof ExecuteStatus.Body) {
            ExecuteStatus.Body body = Casts.cast(entity);
            if (ExecuteStatus.Failure.equals(body.getExecuteStatus())) {
                log.warn("发送 [" + getMessageType() + "] 消息失败,原因:" + body.getException());
            }
        }

        sendMessageComplete(entity);

    }

    public void sendMessageComplete(S entity) {

    }

    public abstract S doSendMessage(Integer id);

    /**
     * 获取最大重试次数
     *
     * @return 重试次数
     */
    protected int getMaxRetryCount() {
        return 0;
    }

    @Autowired
    public void setBatchMessageService(BatchMessageService batchMessageService) {
        this.batchMessageService = batchMessageService;
    }

    @Autowired
    public void setResourceServiceFeignClient(ResourceServiceFeignClient resourceServiceFeignClient) {
        this.resourceServiceFeignClient = resourceServiceFeignClient;
    }

    @Autowired
    @Qualifier("applicationTaskExecutor")
    public void setAsyncTaskExecutor(AsyncTaskExecutor asyncTaskExecutor) {
        this.asyncTaskExecutor = asyncTaskExecutor;
    }

}
