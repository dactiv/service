package com.github.dactiv.service.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.service.authentication.domain.entity.AuthenticationInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_authentication_info 认证信息数据访问
 *
 * <p>Table: tb_authentication_info - 认证信息</p>
 *
 * @author maurice
 * @see AuthenticationInfoEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface AuthenticationInfoDao extends BaseMapper<AuthenticationInfoEntity> {

}
