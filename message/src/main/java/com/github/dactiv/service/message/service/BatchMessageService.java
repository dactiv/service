package com.github.dactiv.service.message.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.service.message.dao.BatchMessageDao;
import com.github.dactiv.service.message.domain.entity.BatchMessageEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_batch_message 的业务逻辑
 *
 * <p>Table: tb_batch_message - 批量消息</p>
 *
 * @author maurice.chen
 * @see BatchMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class BatchMessageService extends BasicService<BatchMessageDao, BatchMessageEntity> {

}
