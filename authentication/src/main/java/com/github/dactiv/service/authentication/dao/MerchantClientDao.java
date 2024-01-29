package com.github.dactiv.service.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.service.authentication.domain.entity.MerchantClientEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_merchant_client 商家 OAuth 2 客户端注册信息的数据访问
 *
 * <p>Table: tb_merchant_client 商家 OAuth 2 客户端注册信息- </p>
 *
 * @author maurice.chen
 * @see MerchantClientEntity
 * @since 2023-11-22 03:21:13
 */
@Mapper
@Repository
public interface MerchantClientDao extends BaseMapper<MerchantClientEntity> {

}
