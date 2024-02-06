package com.github.dactiv.service.resource.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.cipher.AesCipherService;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.resource.config.ApplicationConfig;
import com.github.dactiv.service.resource.dao.MerchantDao;
import com.github.dactiv.service.resource.domain.entity.MerchantEntity;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

    private final AmqpTemplate amqpTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(MerchantEntity entity) {
        Assert.isTrue(!lambdaQuery().eq(MerchantEntity::getName, entity.getName()).exists(), "商户 [" + entity.getName() + "] 已存在。");
        AesCipherService cipherService = cipherAlgorithmService.getCipherService(CipherAlgorithmService.AES_ALGORITHM);
        String text = entity.getName() + CacheProperties.DEFAULT_SEPARATOR + System.currentTimeMillis();
        String appId = DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
        entity.setAppId(appId);
        Key key = cipherService.generateKey();
        entity.setAppKey(Base64.encodeToString(key.getEncoded()));
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(MerchantEntity entity) {
        int result = super.save(entity);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                amqpTemplate.convertAndSend(
                        SystemConstants.RESOURCE_RABBIT_EXCHANGE,
                        SystemConstants.MERCHANT_DATA_CHANGE_QUEUE_NAME,
                        entity
                );
            }
        });

        return result;
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

    @Override
    public int deleteByEntity(MerchantEntity entity) {
        int result = super.deleteByEntity(entity);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                amqpTemplate.convertAndSend(
                        SystemConstants.RESOURCE_RABBIT_EXCHANGE,
                        SystemConstants.MERCHANT_DATA_DELETE_QUEUE_NAME,
                        entity.getId()
                );
            }
        });
        return result;
    }

    @Override
    public int deleteById(Serializable id) {
        int result = super.deleteById(id);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                amqpTemplate.convertAndSend(
                        SystemConstants.RESOURCE_RABBIT_EXCHANGE,
                        SystemConstants.MERCHANT_DATA_DELETE_QUEUE_NAME,
                        id
                );
            }
        });

        return result;
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
