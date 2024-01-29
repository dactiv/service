package com.github.dactiv.service.authentication.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.service.authentication.config.ApplicationConfig;
import com.github.dactiv.service.authentication.dao.MerchantClientDao;
import com.github.dactiv.service.authentication.domain.entity.MerchantClientEntity;
import com.github.dactiv.service.authentication.domain.entity.SsoApplicationEntity;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.feign.ResourceServiceFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;

/**
 * tb_merchant_client 商家 OAuth 2 客户端注册信息的业务逻辑
 *
 * <p>Table: tb_merchant_client 商家 OAuth 2 客户端注册信息- </p>
 *
 * @author maurice.chen
 * @see MerchantClientEntity
 * @since 2023-11-22 03:21:13
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class MerchantClientService extends BasicService<MerchantClientDao, MerchantClientEntity> implements RegisteredClientRepository {

    private final ApplicationConfig applicationConfig;

    private final PasswordEncoder passwordEncoder;

    private final SsoApplicationService ssoApplicationService;

    private final ResourceServiceFeignClient resourceServiceFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(RegisteredClient registeredClient) {
        MerchantClientEntity entity = MerchantClientEntity.ofRegisteredClient(registeredClient);
        this.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        return Objects.requireNonNull(get(id), "找不到 ID 为 [" + id + "] 的商户客户端信息").toRegisteredClient();
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        MerchantClientEntity entity = lambdaQuery()
                .eq(MerchantClientEntity::getClientId, clientId)
                .one();

        Assert.notNull(entity, "找不到 ID 为 [" + clientId + "] 的商户客户端信息");

        Assert.isTrue(YesOrNo.Yes.equals(entity.getEnable()), "ID 为 [" + clientId + "] 的商户客户端已被禁用");

        return entity.toRegisteredClient();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        int result = ids.stream().mapToInt(this::deleteById).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除 id 为 [" + ids + "] 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Serializable id) {
        return super.deleteByEntity(get(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(MerchantClientEntity entity) {
        findSsoApplication(entity.getId()).forEach(sso -> this.deleteSsoApplication(sso, entity));
        return super.deleteByEntity(entity);
    }

    public MerchantClientEntity getByMerchantId(Integer merchantId) {
        return lambdaQuery().eq(MerchantClientEntity::getMerchantId, merchantId).one();
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteByMerchantId(List<Integer> ids) {
        lambdaQuery().in(MerchantClientEntity::getMerchantId, ids).list().forEach(this::deleteById);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdateMerchantClient(MerchantClientEntity entity) {
        if (StringUtils.isEmpty(entity.getClientId())) {
            return;
        }

        if (StringUtils.isEmpty(entity.getClientSecret())) {
            return;
        }

        if (Objects.isNull(entity.getMerchantId())) {
            return;
        }

        entity.setClientSecret(passwordEncoder.encode(entity.getClientSecret()));

        MerchantClientEntity orm = get(entity.getId());

        if (Objects.nonNull(orm)) {
            BeanUtils.copyProperties(entity, orm, MerchantClientEntity.IGNORE_PROPERTIES);
            this.updateById(orm);
        } else {
            long time = System.currentTimeMillis() + applicationConfig.getOauth2SecretKeyExpiresTime().toMillis();
            entity.setClientSecretExpiresAt(new Date(time));
            this.insert(entity);
        }
    }

    public String getMerchantAppKey(String clientId) {
        MerchantClientEntity entity = get(clientId);
        if (Objects.isNull(entity)) {
            return null;
        }

        Map<String, Object> map = resourceServiceFeignClient.getMerchant(entity.getMerchantId());
        if (MapUtils.isEmpty(map)) {
            return null;
        }

        return map.getOrDefault(SystemConstants.APP_KEY_FIELD_NAME, StringUtils.EMPTY).toString();
    }

    // ----------------------------- sso 应用管理 ----------------------------- //

    public List<SsoApplicationEntity> findSsoApplication(String merchantClientId) {
        return ssoApplicationService.lambdaQuery().eq(SsoApplicationEntity::getMerchantClientId, merchantClientId).list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSsoApplication(List<Integer> ids) {
        ids.stream().map(ssoApplicationService::get).forEach(sso -> this.deleteSsoApplication(sso, null));
    }

    public void deleteSsoApplication(SsoApplicationEntity entity, MerchantClientEntity merchantClient) {

        if (Objects.isNull(merchantClient)) {
            merchantClient = Objects.requireNonNull(get(entity.getMerchantClientId()), "找不到 ID 为 [" + entity.getMerchantClientId() + "] 的");
            String url = StringUtils.substringBefore(entity.getUrl(), Casts.QUESTION_MARK);
            merchantClient.getRedirectUris().remove(url);
            updateById(merchantClient);
        }

        ssoApplicationService.deleteByEntity(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveSsoApplication(SsoApplicationEntity entity) {
        if (Objects.isNull(entity.getId())) {
            insertSsoApplication(entity);
        } else {
            updateSsoApplication(entity);
        }
    }

    private void updateSsoApplication(SsoApplicationEntity entity) {
        MerchantClientEntity merchantClient = Objects.requireNonNull(
                get(entity.getMerchantClientId()),
                "找不到 ID 为 [" + entity.getMerchantClientId() + "] 的商户客户端"
        );

        SsoApplicationEntity exist = ssoApplicationService.get(entity.getId());

        String existUrl = StringUtils.substringBefore(exist.getUrl(), Casts.QUESTION_MARK);
        String newUrl = StringUtils.substringBefore(entity.getUrl(), Casts.QUESTION_MARK);

        if (!StringUtils.equals(existUrl, newUrl)) {
            merchantClient.getRedirectUris().remove(existUrl);
            merchantClient.getRedirectUris().add(newUrl);
            updateById(merchantClient);
        }

        ssoApplicationService.updateById(entity);
    }

    private void insertSsoApplication(SsoApplicationEntity entity) {
        MerchantClientEntity merchantClient = Objects.requireNonNull(
                get(entity.getMerchantClientId()),
                "找不到 ID 为 [" + entity.getMerchantClientId() + "] 的商户客户端"
        );

        String url = StringUtils.substringBefore(entity.getUrl(), Casts.QUESTION_MARK);

        int before = merchantClient.getRedirectUris().size();
        merchantClient.getRedirectUris().add(url);
        int after = merchantClient.getRedirectUris().size();

        if (before < after) {
            updateById(merchantClient);
        }

        ssoApplicationService.insert(entity);
    }

    public Page<SsoApplicationEntity> findSsoApplicationPage(PageRequest pageRequest, QueryWrapper<SsoApplicationEntity> query) {
        return ssoApplicationService.findTotalPage(pageRequest, query);
    }

    public List<SsoApplicationEntity> findSsoApplication(QueryWrapper<SsoApplicationEntity> query) {
        return ssoApplicationService.find(query);
    }

    public SsoApplicationEntity getSsoApplication(Integer id) {
        return ssoApplicationService.get(id);
    }
}
