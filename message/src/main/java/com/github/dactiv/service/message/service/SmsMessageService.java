package com.github.dactiv.service.message.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import com.github.dactiv.service.message.dao.SmsMessageDao;
import com.github.dactiv.service.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.service.message.resolver.MessageTypeResolver;
import com.github.dactiv.service.message.service.support.SmsMessageSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * tb_sms_message 的业务逻辑
 *
 * <p>Table: tb_sms_message - 短信消息</p>
 *
 * @author maurice.chen
 * @see SmsMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class SmsMessageService extends BasicService<SmsMessageDao, SmsMessageEntity> implements MessageTypeResolver {

    @Override
    public String getCategory() {
        return SmsMessageSender.DEFAULT_TYPE;
    }

    @Override
    public List<MessageTypeEnum> getMessageTypeList(TypeUserDetails<Integer> userDetails) {
        return Arrays.asList(MessageTypeEnum.SYSTEM, MessageTypeEnum.NOTICE, MessageTypeEnum.WARNING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(SmsMessageEntity entity) {
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(SmsMessageEntity entity) {
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(SmsMessageEntity entity) {
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        return super.deleteById(ids, errorThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(Collection<SmsMessageEntity> entities, boolean errorThrow) {
        return super.deleteByEntity(entities, errorThrow);
    }
}
