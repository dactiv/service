package com.github.dactiv.service.resource.service.dictionary;

import com.alibaba.nacos.api.common.Constants;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.service.commons.service.domain.meta.ResourceDictionaryMeta;
import com.github.dactiv.service.resource.dao.dictionary.DataDictionaryDao;
import com.github.dactiv.service.resource.domain.entity.dictionary.DataDictionaryEntity;
import com.github.dactiv.service.resource.domain.meta.DataDictionaryMeta;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * tb_data_dictionary 的业务逻辑
 *
 * <p>Table: tb_data_dictionary - 数据字典表</p>
 *
 * @author maurice.chen
 * @see DataDictionaryEntity
 * @since 2021-12-09 11:28:04
 */
@Service
@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.NOT_SUPPORTED)
public class DataDictionaryService extends BasicService<DataDictionaryDao, DataDictionaryEntity> {

    /**
     * 获取数据字典
     *
     * @param code 代码
     * @return 数据字典
     */
    public DataDictionaryEntity getByCode(String code) {
        return lambdaQuery().eq(DataDictionaryEntity::getCode, code).one();
    }

    /**
     * 获取数据字典集合
     *
     * @param parentId 父类 id
     * @return 数据字典集合
     */
    public List<DataDictionaryEntity> findByParentId(Integer parentId) {
        return lambdaQuery().eq(DataDictionaryEntity::getParentId, parentId).list();
    }

    /**
     * 获取数据字典集合
     *
     * @param typeId 字典类型 id
     * @return 数据字典集合
     */
    public List<DataDictionaryEntity> findByTypeId(Integer typeId) {
        return lambdaQuery().eq(DataDictionaryEntity::getTypeId, typeId).list();
    }

    public List<DataDictionaryMeta> findDataDictionaryMetas(String code) {

        return findByCode(code)
                .stream()
                .map(e -> Casts.of(e, DataDictionaryMeta.class))
                .collect(Collectors.toList());
    }

    public List<DataDictionaryEntity> findByCode(String code) {
        int index = StringUtils.indexOf(code, Constants.ALL_PATTERN);

        LambdaQueryChainWrapper<DataDictionaryEntity> wrapper = lambdaQuery().select(
                DataDictionaryEntity::getName,
                DataDictionaryEntity::getValue,
                DataDictionaryEntity::getId,
                ResourceDictionaryMeta::getCode,
                DataDictionaryEntity::getParentId,
                DataDictionaryEntity::getEnabled,
                DataDictionaryMeta::getMeta,
                DataDictionaryEntity::getValueType,
                DataDictionaryEntity::getLevel
        );

        if (index > 0) {
            wrapper.likeRight(DataDictionaryEntity::getCode, StringUtils.substring(code, 0, index));
        } else {
            wrapper.eq(DataDictionaryEntity::getCode, code);
        }

        wrapper
                .eq(DataDictionaryEntity::getEnabled, DisabledOrEnabled.Enabled.getValue())
                .orderByAsc(DataDictionaryEntity::getSort);

        List<DataDictionaryEntity> result = wrapper.list();
        result.forEach(e -> e.setValue(Casts.cast(e.getValue(), e.getValueType().getClassType())));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(DataDictionaryEntity entity) {
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(DataDictionaryEntity entity) {
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(DataDictionaryEntity entity) {
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        return super.deleteById(ids, errorThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(Collection<DataDictionaryEntity> entities, boolean errorThrow) {
        return super.deleteByEntity(entities, errorThrow);
    }
}
