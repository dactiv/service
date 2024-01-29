package com.github.dactiv.service.authentication.plugin.support;

import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.service.authentication.config.ApplicationConfig;
import com.github.dactiv.service.authentication.domain.entity.GroupEntity;
import com.github.dactiv.service.authentication.domain.meta.ResourceMeta;
import com.github.dactiv.service.authentication.plugin.PluginInstance;
import com.github.dactiv.service.authentication.plugin.PluginResourceInterceptor;
import com.github.dactiv.service.authentication.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 同步超级管理员组资源的拦截器实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class SyncAdminGroupResourceInterceptor implements PluginResourceInterceptor {

    private final ApplicationConfig applicationConfig;

    private final GroupService groupService;

    @Override
    public void postSyncPlugin(PluginInstance instance, List<ResourceMeta> newResourceList) {

        GroupEntity group = groupService.get(applicationConfig.getAdminGroupId());

        // 如果配置了管理员组 线删除同步一次管理员資源
        if (Objects.isNull(group)) {
            return;
        }

        List<String> newResourceIds = newResourceList
                .stream()
                .filter(r -> r.getSources().stream().anyMatch(s -> group.getSources().contains(s)))
                .map(IdEntity::getId)
                .collect(Collectors.toList());

        if (MapUtils.isEmpty(group.getResourceMap())) {
            group.setResourceMap(new LinkedHashMap<>());
        }
        // 覆盖当前应用的资源
        group.getResourceMap().put(instance.getServiceName(), newResourceIds);
        groupService.save(group);

    }

    @Override
    public void postDisabledApplicationResource(NacosService nacosService) {
        GroupEntity group = groupService.get(applicationConfig.getAdminGroupId());

        // 如果配置了管理员组 线删除同步一次管理员資源
        if (Objects.isNull(group)) {
            return;
        }
        // 移除当前应用的资源
        group.getResourceMap().remove(nacosService.getName());
        groupService.save(group);
    }
}
