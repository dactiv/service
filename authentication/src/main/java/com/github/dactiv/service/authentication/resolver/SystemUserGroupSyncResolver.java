package com.github.dactiv.service.authentication.resolver;

import com.github.dactiv.service.authentication.domain.entity.GroupEntity;
import com.github.dactiv.service.authentication.domain.entity.SystemUserEntity;

import java.util.List;

/**
 * 系统用户角色发生变更解析器
 *
 * @author maurice.chen
 */
public interface SystemUserGroupSyncResolver {

    void syncSystemUserGroup(SystemUserEntity sue, List<GroupEntity> groups);
}
