package com.github.dactiv.service.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.service.message.domain.entity.SiteMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_site_message 站内信消息数据访问
 *
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice
 * @see SiteMessageEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface SiteMessageDao extends BaseMapper<SiteMessageEntity> {
}
