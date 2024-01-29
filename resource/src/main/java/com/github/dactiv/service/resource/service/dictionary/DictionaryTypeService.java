package com.github.dactiv.service.resource.service.dictionary;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.service.resource.dao.dictionary.DictionaryTypeDao;
import com.github.dactiv.service.resource.domain.entity.dictionary.DictionaryTypeEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * tb_dictionary_type 的业务逻辑
 *
 * <p>Table: tb_dictionary_type - 数据字典类型表</p>
 *
 * @author maurice.chen
 * @see DictionaryTypeEntity
 * @since 2021-12-09 11:28:04
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DictionaryTypeService extends BasicService<DictionaryTypeDao, DictionaryTypeEntity> {

    /**
     * 获取数据字典
     *
     * @param code 代码
     * @return 数据字典
     */
    public DictionaryTypeEntity getByCode(String code) {
        return lambdaQuery().eq(DictionaryTypeEntity::getCode, code).one();
    }

    /**
     * 获取数据字典集合
     *
     * @param parentId 父类 id
     * @return 数据字典集合
     */
    public List<DictionaryTypeEntity> getByParentId(Integer parentId) {
        return lambdaQuery().eq(DictionaryTypeEntity::getParentId, parentId).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(DictionaryTypeEntity entity) {
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(DictionaryTypeEntity entity) {
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(DictionaryTypeEntity entity) {
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        return super.deleteById(ids, errorThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(Collection<DictionaryTypeEntity> entities, boolean errorThrow) {
        return super.deleteByEntity(entities, errorThrow);
    }

}
