package com.github.dactiv.service.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.service.authentication.domain.entity.AuthorizationConsentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_authorization_consent 的数据访问
 *
 * <p>Table: tb_authorization_consent - 商户授权同意用户信息</p>
 *
 * @author maurice.chen
 * @see AuthorizationConsentEntity
 * @since 2023-11-28 03:46:21
 */
@Mapper
@Repository
public interface AuthorizationConsentDao extends BaseMapper<AuthorizationConsentEntity> {

}
