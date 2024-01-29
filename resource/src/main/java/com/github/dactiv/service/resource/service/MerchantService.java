package com.github.dactiv.service.resource.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.cipher.AesCipherService;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.service.resource.config.ApplicationConfig;
import com.github.dactiv.service.resource.dao.MerchantDao;
import com.github.dactiv.service.resource.domain.entity.MerchantEntity;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Objects;

/**
 * tb_merchant 的业务逻辑
 *
 * <p>Table: tb_merchant - 商户表</p>
 *
 * @author maurice.chen
 * @see MerchantEntity
 * @since 2023-09-11 08:57:11
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.NOT_SUPPORTED)
public class MerchantService extends BasicService<MerchantDao, MerchantEntity> {

    private final CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

    private final ApplicationConfig applicationConfig;

    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(MerchantEntity entity) {
        Assert.isTrue(!lambdaQuery().eq(MerchantEntity::getName, entity.getName()).exists(), "商户 [" + entity.getName() + "] 已存在。");
        AesCipherService cipherService = cipherAlgorithmService.getCipherService(CipherAlgorithmService.AES_ALGORITHM);
        String text = entity.getName() + CacheProperties.DEFAULT_SEPARATOR + System.currentTimeMillis();
        String appId = DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
        entity.setAppId(appId);
        Key key = cipherService.generateKey(applicationConfig.getMerchantAesKeySize());
        entity.setAppKey(Base64.encodeToString(key.getEncoded()));
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(MerchantEntity entity) {
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(MerchantEntity entity) {
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        return super.deleteById(ids, errorThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(Collection<MerchantEntity> entities, boolean errorThrow) {
        return super.deleteByEntity(entities, errorThrow);
    }

    private RBucket<MerchantEntity> getRedissonBucket(String appId) {
        return redissonClient.getBucket(applicationConfig.getMerchantCache().getName(appId));
    }

    public MerchantEntity loadMerchant(String appId) {

        RBucket<MerchantEntity> bucket = getRedissonBucket(appId);
        MerchantEntity entity = bucket.get();
        if (Objects.isNull(entity)) {

            entity = getByAppId(appId);
            Assert.notNull(entity, "找不到 APP ID 为 [" + appId + "] 的商户信息");

            CacheProperties cacheProperties = applicationConfig.getMerchantCache();
            if (Objects.isNull(cacheProperties)) {
                return entity;
            }

            TimeProperties time = cacheProperties.getExpiresTime();
            if (Objects.nonNull(time)) {
                bucket.setAsync(entity, time.getValue(), time.getUnit());
            } else {
                bucket.setAsync(entity);
            }

        }
        return entity;
    }

    private MerchantEntity getByAppId(String appId) {
        return lambdaQuery().eq(MerchantEntity::getAppId, appId).one();
    }
}
