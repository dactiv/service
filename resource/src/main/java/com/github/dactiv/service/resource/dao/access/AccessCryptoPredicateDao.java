package com.github.dactiv.service.resource.dao.access;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.service.resource.domain.entity.access.AccessCryptoPredicateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_access_crypto_predicate 访问加解密断言数据访问
 *
 * <p>Table: tb_access_crypto_predicate - 访问加解密断言</p>
 *
 * @author maurice
 * @see AccessCryptoPredicateEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface AccessCryptoPredicateDao extends BaseMapper<AccessCryptoPredicateEntity> {

}
