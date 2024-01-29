package com.github.dactiv.service.resource.service.access;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.service.resource.dao.access.AccessCryptoPredicateDao;
import com.github.dactiv.service.resource.domain.entity.access.AccessCryptoPredicateEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;

/**
 * tb_access_crypto_predicate 的业务逻辑
 *
 * <p>Table: tb_access_crypto_predicate - 访问加解密条件表</p>
 *
 * @author maurice.chen
 * @see AccessCryptoPredicateEntity
 * @since 2021-12-09 11:28:04
 */
@Service
@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.NOT_SUPPORTED)
public class AccessCryptoPredicateService extends BasicService<AccessCryptoPredicateDao, AccessCryptoPredicateEntity> {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(AccessCryptoPredicateEntity entity) {
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(AccessCryptoPredicateEntity entity) {
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(AccessCryptoPredicateEntity entity) {
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        return super.deleteById(ids, errorThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(Collection<AccessCryptoPredicateEntity> entities, boolean errorThrow) {
        return super.deleteByEntity(entities, errorThrow);
    }

}
