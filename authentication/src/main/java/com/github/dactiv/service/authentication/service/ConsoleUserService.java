package com.github.dactiv.service.authentication.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.authentication.OidcScopesConstants;
import com.github.dactiv.service.authentication.dao.ConsoleUserDao;
import com.github.dactiv.service.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.service.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.service.authentication.resolver.SystemUserResolver;
import com.github.dactiv.service.commons.service.SystemErrorCodeConstants;
import com.github.dactiv.service.commons.service.config.CommonsConfig;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_console_user 的业务逻辑
 *
 * <p>Table: tb_console_user - 后台用户表</p>
 *
 * @author maurice.chen
 * @see ConsoleUserEntity
 * @since 2021-11-25 02:42:57
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Lazy)
@Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class ConsoleUserService extends BasicService<ConsoleUserDao, ConsoleUserEntity> implements SystemUserResolver {

    @Lazy
    @Getter
    private final AuthorizationService authorizationService;

    private final MerchantClientService merchantClientService;

    private final MybatisPlusQueryGenerator<ConsoleUserEntity> queryGenerator;

    private final CommonsConfig commonsConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        int result = ids.stream().mapToInt(this::deleteById).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除后台用户 ID 为[" + ids + " 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(Collection<ConsoleUserEntity> entities, boolean errorThrow) {

        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "删除后台用户为 " + entities + " 的 [" + getEntityClass() + "] 数据不成功";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(ConsoleUserEntity entity) {
        authorizationService.deleteSystemUserAllCache(entity, ResourceSourceEnum.CONSOLE);
        return super.deleteByEntity(entity);
    }

    @Override
    public int delete(Wrapper<ConsoleUserEntity> wrapper) {
        throw new UnsupportedOperationException(this.getClass().getName() + "不支持 delete(Wrapper<ConsoleUserEntity> wrapper) 操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Serializable id) {
        ConsoleUserEntity user = get(id);
        return deleteByEntity(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(ConsoleUserEntity entity) {
        authorizationService.deleteSystemUserAllCache(entity, ResourceSourceEnum.CONSOLE);
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ConsoleUserEntity entity) {
        Assert.hasText(entity.getUsername(), "登陆账户不能为空");
        boolean usernameExist = lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getUsername, entity.getUsername())
                .exists();

        if (usernameExist) {
            throw new ErrorCodeException("登录账户 [" + entity.getUsername() + "] 已存在", SystemErrorCodeConstants.CONTENT_EXIST);
        }

        boolean emailExist = lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getEmail, entity.getEmail())
                .exists();

        if (emailExist) {
            throw new ErrorCodeException("邮箱账户 [" + entity.getEmail() + "] 已存在", SystemErrorCodeConstants.CONTENT_EXIST);
        }

        PasswordEncoder passwordEncoder = authorizationService
                .getUserDetailsService(ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
                .getPasswordEncoder();

        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return super.insert(entity);
    }

    /**
     * 通过登录账号或邮箱获取后台用户
     *
     * @param identity 登录账号或邮箱
     * @return 后台用户
     */
    @Override
    public ConsoleUserEntity getByIdentity(String identity) {
        return lambdaQuery()
                .eq(ConsoleUserEntity::getUsername, identity)
                .or()
                .eq(ConsoleUserEntity::getEmail, identity)
                .one();
    }

    @Override
    public SystemUserEntity convertTargetUser(TypeUserDetails<Integer> userDetails) {
        if (Objects.nonNull(userDetails.getUserId())) {
            return get(Casts.cast(userDetails.getUserId(), Integer.class));
        } else if (StringUtils.isNotEmpty(userDetails.getUsername())) {
            return getByIdentity(userDetails.getUsername());
        }

        return null;
    }

    @Override
    public List<SystemUserEntity> getUserByGroupId(List<Integer> ids) {
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        filter.add("filter_[groups_info.id_jin]", ids.stream().map(Object::toString).collect(Collectors.toList()));
        return new LinkedList<>(find(queryGenerator.createQueryWrapperFromMap(filter)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SystemUserEntity user) {
        updateById(Casts.cast(user, ConsoleUserEntity.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(SystemUserEntity user) {
        insert(Casts.cast(user, ConsoleUserEntity.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SystemUserEntity user) {
        save(Casts.cast(user, ConsoleUserEntity.class));
    }

    @Override
    public void createOidcUserInfo(OAuth2Authorization oAuth2Authorization,
                                   Map<String, Object> claims,
                                   SecurityUserDetails userDetails) {

        String appKey = merchantClientService.getMerchantAppKey(oAuth2Authorization.getRegisteredClientId());
        if (StringUtils.isEmpty(appKey)) {
            return;
        }

        OidcScopesConstants.createOidcUserInfoClaims(
                oAuth2Authorization,
                claims,
                commonsConfig,
                appKey,
                userDetails
        );
    }

    @Override
    public boolean isSupport(String type) {
        return ResourceSourceEnum.CONSOLE_SOURCE_VALUE.equals(type);
    }


}
