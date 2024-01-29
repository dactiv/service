package com.github.dactiv.service.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.service.authentication.domain.entity.SsoApplicationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_sso_application 的数据访问
 *
 * <p>Table: tb_sso_application - 单点登陆应用</p>
 *
 * @author maurice.chen
 * @see SsoApplicationEntity
 * @since 2023-11-29 10:22:33
 */
@Mapper
@Repository
public interface SsoApplicationDao extends BaseMapper<SsoApplicationEntity> {

}
