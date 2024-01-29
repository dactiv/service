package com.github.dactiv.service.resource.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.service.resource.domain.entity.MerchantEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_merchant 的数据访问
 *
 * <p>Table: tb_merchant - 商户表</p>
 *
 * @author maurice.chen
 * @see MerchantEntity
 * @since 2023-09-11 08:57:11
 */
@Mapper
@Repository
public interface MerchantDao extends BaseMapper<MerchantEntity> {

}
