package com.github.dactiv.service.message.service;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import com.github.dactiv.service.message.dao.EmailMessageDao;
import com.github.dactiv.service.message.domain.entity.EmailMessageEntity;
import com.github.dactiv.service.message.resolver.MessageTypeResolver;
import com.github.dactiv.service.message.service.attachment.AttachmentResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * tb_email_message 的业务逻辑
 *
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @author maurice.chen
 * @see EmailMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.NOT_SUPPORTED)
public class EmailMessageService extends BasicService<EmailMessageDao, EmailMessageEntity> implements AttachmentResolver, MessageTypeResolver {

    @Override
    public String getMessageType() {
        return MessageServiceFeignClient.DEFAULT_EMAIL_TYPE_VALUE;
    }

    @Override
    public RestResult<Object> removeAttachment(Integer id, FileObject fileObject) {
        EmailMessageEntity entity = get(id);
        entity.getAttachmentList().removeIf(a -> a.getBucketName().equals(fileObject.getBucketName()) && a.getObjectName().equals(fileObject.getObjectName()));
        save(entity);
        return RestResult.ofSuccess("删除附件成功", entity);
    }

    @Override
    public String getCategory() {
        return getMessageType();
    }

    @Override
    public List<MessageTypeEnum> getMessageTypeList(TypeUserDetails<Integer> userDetails) {
        return Arrays.asList(MessageTypeEnum.SYSTEM, MessageTypeEnum.NOTICE, MessageTypeEnum.WARNING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(EmailMessageEntity entity) {
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(EmailMessageEntity entity) {
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(EmailMessageEntity entity) {
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        return super.deleteById(ids, errorThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(Collection<EmailMessageEntity> entities, boolean errorThrow) {
        return super.deleteByEntity(entities, errorThrow);
    }
}
