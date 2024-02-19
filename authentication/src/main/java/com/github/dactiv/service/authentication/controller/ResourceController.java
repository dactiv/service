package com.github.dactiv.service.authentication.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.service.authentication.domain.meta.ResourceMeta;
import com.github.dactiv.service.authentication.service.AuthorizationService;
import com.github.dactiv.service.authentication.service.ConsoleUserService;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源控制器
 *
 * @author maurice.chen
 **/
@RestController
@RequestMapping("resource")
@Plugin(
        name = "权限管理",
        id = "resource",
        parent = "authority",
        icon = "icon-classification-type",
        authority = "perms[resource:find]",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class ResourceController {

    private final AuthorizationService authorizationService;

    private final ConsoleUserService consoleUserService;

    /**
     * 查找资源
     *
     * @param mergeTree 合并树行
     * @return 资源实体集合
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[authentication_resource:find]')")
    public List<ResourceMeta> find(@RequestParam(required = false, defaultValue = "true") boolean mergeTree,
                                   @RequestParam(required = false) String applicationName,
                                   @RequestParam(required = false) List<String> sources) {

        List<ResourceSourceEnum> resourceSources = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(sources)) {
            resourceSources = sources
                    .stream()
                    .map(s -> NameEnumUtils.parse(s, ResourceSourceEnum.class))
                    .collect(Collectors.toList());
        }

        List<ResourceMeta> resourceList = authorizationService.getResources(
                applicationName,
                resourceSources.toArray(new ResourceSourceEnum[0])
        );

        if (mergeTree) {
            return TreeUtils.buildGenericTree(resourceList);
        } else {
            return resourceList;
        }
    }

    /**
     * 获取用户关联资源实体集合
     *
     * @param userId 用户主键值
     * @return 资源实体集合
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("getConsoleUserResources")
    public List<String> getConsoleUserResources(@RequestParam Integer userId) {
        ConsoleUserEntity systemUser = consoleUserService.get(userId);
        Set<Map.Entry<String, List<String>>> entrySet = systemUser.getResourceMap().entrySet();
        return entrySet.stream().flatMap(e -> e.getValue().stream()).collect(Collectors.toList());
    }

    /**
     * 获取当前用户资源
     *
     * @param securityContext 安全上下文
     * @param mergeTree       是否合并树形 true，是 否则 false
     * @return 资源实体集合
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("getConsolePrincipalResources")
    public List<ResourceMeta> getConsolePrincipalResources(@CurrentSecurityContext SecurityContext securityContext,
                                                           @RequestParam(required = false) String type,
                                                           @RequestParam(required = false, defaultValue = "false") boolean mergeTree) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        List<ResourceSourceEnum> sourceContains = Arrays.asList(
                NameEnumUtils.parse(userDetails.getType(), ResourceSourceEnum.class),
                ResourceSourceEnum.SYSTEM
        );

        ResourceType resourceType = null;
        if (StringUtils.isNotBlank(type)) {
            resourceType = NameEnumUtils.parse(type, ResourceType.class);
        }

        List<ResourceMeta> resourceList = authorizationService.getSystemUserResource(
                userDetails,
                resourceType,
                sourceContains
        );

        List<ResourceMeta> result = resourceList
                .stream()
                .sorted(Comparator.comparing(ResourceMeta::getSort).reversed())
                .collect(Collectors.toList());

        if (mergeTree) {
            return TreeUtils.buildGenericTree(result);
        } else {
            return result;
        }
    }

    /**
     * 获取资源
     *
     * @param id 主键值
     * @return 资源实体
     */
    @GetMapping("get")
    @Plugin(name = "查看信息")
    @PreAuthorize("hasAuthority('perms[authentication_resource:get]')")
    public ResourceMeta get(@RequestParam String id) {

        return authorizationService
                .getResources(null)
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 同步插件資源
     *
     * @return reset 结果集
     */
    @PostMapping("syncPluginResource")
    @Plugin(name = "同步插件资源", audit = true)
    @PreAuthorize("hasAuthority('perms[authentication_resource:sync_plugin_resource]')")
    public RestResult<?> syncPluginResource() {
        authorizationService.getPluginResourceService().resubscribeAllService();
        return RestResult.of("同步数据完成");
    }
}
