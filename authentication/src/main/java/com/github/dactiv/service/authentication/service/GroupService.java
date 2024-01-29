package com.github.dactiv.service.authentication.service;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.service.authentication.config.ApplicationConfig;
import com.github.dactiv.service.authentication.dao.GroupDao;
import com.github.dactiv.service.authentication.domain.entity.GroupEntity;
import com.github.dactiv.service.authentication.domain.meta.ResourceMeta;
import com.github.dactiv.service.commons.service.domain.meta.IdRoleAuthorityMeta;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * tb_group 的业务逻辑
 *
 * <p>Table: tb_group - 用户组表</p>
 *
 * @author maurice.chen
 * @see GroupEntity
 * @since 2021-11-25 02:42:57
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class GroupService extends BasicService<GroupDao, GroupEntity> {

    private final AuthorizationService authorizationService;

    private final ApplicationConfig config;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(GroupEntity entity) {

        List<ResourceMeta> groupResource = authorizationService.getGroupResource(entity);

        List<String> noneMatchSources = groupResource
                .stream()
                .filter(r -> r.getSources().stream().noneMatch(s -> entity.getSources().contains(s)))
                .distinct()
                .flatMap(r -> r.getSources().stream().filter(s -> !entity.getSources().contains(s)))
                .map(ResourceSourceEnum::getName)
                .toList();

        if (!noneMatchSources.isEmpty()) {

            List<String> sourceNames = entity
                    .getSources()
                    .stream()
                    .map(ResourceSourceEnum::getName)
                    .toList();

            throw new ServiceException("组来源 " + sourceNames + " 不能保存属于 " + noneMatchSources + " 的资源");
        }

        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(GroupEntity entity) {
        GroupEntity exist = lambdaQuery()
                .eq(GroupEntity::getName, entity.getName())
                .or()
                .eq(GroupEntity::getAuthority, entity.getAuthority())
                .one();

        if (Objects.nonNull(exist)) {
            throw new ServiceException("用户组名称 [" + entity.getName() + "] 或 authority 值 [" + entity.getAuthority() + "] 已存在");
        }

        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(GroupEntity entity) {
        GroupEntity exist = get(entity.getId());

        if (YesOrNo.No.equals(exist.getModifiable())) {
            throw new ServiceException("角色 [" + exist.getName() + "] 不可修改");
        }

        int result = super.updateById(entity);
        authorizationService.deleteAuthorizationCache(exist.getSources());

        authorizationService.syncSystemUserGroup(Collections.singletonList(entity), (sue, groups) -> {
            List<Integer> groupIds = groups.stream().map(IdEntity::getId).toList();

            sue.getGroupsInfo().removeIf(p -> groupIds.contains(p.getId()));

            List<IdRoleAuthorityMeta> idRoleAuthorityMetas = groups
                    .stream()
                    .map(g -> IdRoleAuthorityMeta.of(g.getId(), g.getName(), g.getAuthority(), g.getStatus()))
                    .toList();

            sue.getGroupsInfo().addAll(idRoleAuthorityMetas);
        });

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {

        List<GroupEntity> groups = get(ids);

        List<GroupEntity> unRemovables = groups
                .stream()
                .filter(g -> YesOrNo.No.equals(g.getRemovable()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(unRemovables)) {
            throw new ServiceException("用户组 " + unRemovables.stream().map(GroupEntity::getName).toList() + " 不可删除");
        }

        if (groups.stream().anyMatch(g -> g.getId().equals(config.getAdminGroupId()))) {
            throw new ServiceException("不能删除管理员组");
        }

        authorizationService.syncSystemUserGroup(groups, (sue, groupEntityList) -> {
            List<Integer> groupIds = groupEntityList.stream().map(IdEntity::getId).toList();
            sue.getGroupsInfo().removeIf(p -> groupIds.contains(p.getId()));
        });

        return super.deleteById(ids, errorThrow);
    }
}
