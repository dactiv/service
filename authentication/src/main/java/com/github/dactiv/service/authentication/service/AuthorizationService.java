package com.github.dactiv.service.authentication.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.jackson.serializer.DesensitizeSerializer;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.security.entity.ResourceAuthority;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.spring.security.authentication.AccessTokenContextRepository;
import com.github.dactiv.framework.spring.security.authentication.UserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.SpringSecurityProperties;
import com.github.dactiv.framework.spring.security.authentication.rememberme.CookieRememberService;
import com.github.dactiv.framework.spring.security.authentication.token.SimpleAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.authentication.domain.PhoneNumberUserDetails;
import com.github.dactiv.service.authentication.domain.entity.GroupEntity;
import com.github.dactiv.service.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.service.authentication.domain.meta.ResourceMeta;
import com.github.dactiv.service.authentication.plugin.PluginResourceService;
import com.github.dactiv.service.authentication.resolver.SystemUserGroupSyncResolver;
import com.github.dactiv.service.authentication.resolver.SystemUserResolver;
import com.github.dactiv.service.commons.service.domain.meta.IdRoleAuthorityMeta;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 授权管理服务
 *
 * @author maurice.chen
 **/
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final List<UserDetailsService> userDetailsServices;

    private final List<SystemUserResolver> systemUserResolvers;

    @Getter
    private final RedissonClient redissonClient;

    private final GroupService groupService;

    @Getter
    private final PluginResourceService pluginResourceService;

    private final RememberMeServices rememberMeServices;

    private final SpringSessionBackedSessionRegistry<? extends Session> sessionBackedSessionRegistry;

    @Getter
    private final AccessTokenContextRepository accessTokenContextRepository;

    @Getter
    private final SpringSecurityProperties springSecurityProperties;

    @Getter
    @Setter
    private List<ResourceMeta> resourceMetas = new LinkedList<>();

    private void expireUserSession(Object user) {
        List<SessionInformation> sessions = sessionBackedSessionRegistry.getAllSessions(user, false);
        sessions.forEach(SessionInformation::expireNow);
    }

    public void deleteSecurityUserDetailsAllCache(TypeUserDetails<Integer> userDetails) {
        SystemUserEntity entity = systemUserResolvers
                .stream()
                .filter(r -> r.isSupport(userDetails.getUserType()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到资源来源为 [" + userDetails.getUserType() + "] 的系统用户解析器实现"))
                .convertTargetUser(userDetails);

        if (Objects.nonNull(entity)) {
            ResourceSourceEnum source = ResourceSourceEnum.parse(userDetails.getUserType());
            deleteSystemUserAllCache(entity, source);
        }
    }

    public void deleteSystemUserAllCache(SystemUserEntity entity, ResourceSourceEnum source) {
        deleteSystemUserAuthenticationCache(entity, source);

        if (rememberMeServices instanceof CookieRememberService) {
            CookieRememberService cookieRememberService = Casts.cast(rememberMeServices);
            TypeUserDetails<Object> typeUserDetails = BasicUserDetails.of(entity.getId(), entity.getUsername(), source.toString());
            cookieRememberService.deleteRememberMeToken(typeUserDetails);
        }

        accessTokenContextRepository.deleteContext(source.toString(), entity.getId());

        expireSystemUserSession(entity, source);
    }

    /**
     * 创建授权 token 流
     *
     * @param user 后台用户实体
     * @return 授权 token 流
     */
    private Stream<SimpleAuthenticationToken> createPrincipalAuthenticationTokenStream(SystemUserEntity user, ResourceSourceEnum source) {
        List<SimpleAuthenticationToken> result = new LinkedList<>();

        result.add(new SimpleAuthenticationToken(user.getUsername(), source.toString(), false));
        result.add(new SimpleAuthenticationToken(user.getEmail(), source.toString(), false));

        if (user instanceof PhoneNumberUserDetails) {
            PhoneNumberUserDetails userDetails = Casts.cast(user);
            result.add(new SimpleAuthenticationToken(userDetails.getPhoneNumber(), source.toString(), false));
        }

        return result.stream();
    }

    /**
     * 获取账户认证的用户明细服务
     *
     * @param userType 用户类型
     * @return 账户认证的用户明细服务
     */
    public UserDetailsService getUserDetailsService(String userType) {
        return userDetailsServices
                .stream()
                .filter(s -> s.getType().contains(userType))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到类型为 [" + userType + "] 的 UserDetailsService 实现"));
    }

    public SystemUserEntity getSystemUser(TypeUserDetails<Integer> userDetails) {
        SystemUserResolver systemUserResolver = getSystemUserResolver(userDetails.getUserType());
        return Casts.cast(systemUserResolver.convertTargetUser(userDetails));
    }

    public SystemUserResolver getSystemUserResolver(String userType) {
        return systemUserResolvers
                .stream()
                .filter(r -> r.isSupport(userType))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到资源来源为 [" + userType + "] 的系统用户解析器实现"));
    }

    /**
     * 获取组资源集合
     *
     * @param group 组信息
     * @return 资源结婚
     */
    public List<ResourceMeta> getGroupResource(GroupEntity group) {
        List<ResourceMeta> result = new LinkedList<>();

        if (MapUtils.isEmpty(group.getResourceMap())) {
            return result;
        }

        for (Map.Entry<String, List<String>> entry : group.getResourceMap().entrySet()) {
            List<ResourceMeta> resources = getResources(entry.getKey());
            List<ResourceMeta> findResources = resources
                    .stream()
                    .filter(r -> entry.getValue().contains(r.getId()))
                    .toList();

            result.addAll(findResources);
        }

        return result;
    }

    /**
     * 删除所有认证缓存
     *
     * @param sources 资源来源枚举
     */
    public void deleteAuthorizationCache(List<ResourceSourceEnum> sources) {

        List<SimpleAuthenticationToken> tokens = sources
                .stream()
                .map(s -> new SimpleAuthenticationToken(DesensitizeSerializer.DEFAULT_DESENSITIZE_SYMBOL, s.toString(), false))
                .toList();

        for (SimpleAuthenticationToken token : tokens) {
            String key = springSecurityProperties.getAuthorizationCache().getName(token.getName());

            redissonClient.getKeys().getKeysByPattern(key).forEach(k -> {
                redissonClient.getBucket(k).deleteAsync();
                String identity = StringUtils.substringAfterLast(k, CacheProperties.DEFAULT_SEPARATOR);

                Optional<SystemUserResolver> optional = systemUserResolvers
                        .stream()
                        .filter(s -> s.isSupport(token.getType()))
                        .findFirst();
                if (optional.isEmpty()) {
                    return;
                }

                SystemUserResolver resolver = optional.get();
                SystemUserEntity entity = resolver.getByIdentity(identity);
                if (Objects.isNull(entity)) {
                    return;
                }

                ResourceSourceEnum source = ResourceSourceEnum.parse(token.getType());
                expireSystemUserSession(entity, source);
            });


        }

    }

    /**
     * 将制定系统用户的 session 过期
     *
     * @param entity 系统用户实体
     * @param source 用户所属来源
     */
    public void expireSystemUserSession(SystemUserEntity entity, ResourceSourceEnum source) {
        List<SimpleAuthenticationToken> tokens = new LinkedList<>();

        SimpleAuthenticationToken usernameToken = new SimpleAuthenticationToken(
                entity.getUsername(),
                source.toString(),
                false
        );
        tokens.add(usernameToken);

        if (StringUtils.isNotEmpty(entity.getEmail())) {
            SimpleAuthenticationToken emailToken = new SimpleAuthenticationToken(
                    entity.getEmail(),
                    source.toString(),
                    false
            );
            tokens.add(emailToken);
        }

        tokens.forEach(this::expireUserSession);

    }

    /**
     * 获取资源集合
     *
     * @param applicationName 应用名称
     * @param sources         符合来源的记录
     * @return 资源集合
     */
    public List<ResourceMeta> getResources(String applicationName, ResourceSourceEnum... sources) {
        List<ResourceMeta> result = pluginResourceService.getResources();
        Stream<ResourceMeta> stream = result.stream();

        if (StringUtils.isNotBlank(applicationName)) {
            stream = stream.filter(r -> r.getApplicationName().equals(applicationName));
        }

        if (ArrayUtils.isNotEmpty(sources)) {
            List<ResourceSourceEnum> sourceList = Arrays.asList(sources);
            stream = stream.filter(r -> r.getSources().stream().anyMatch(sourceList::contains));
        }

        return stream.sorted(Comparator.comparing(ResourceMeta::getSort).reversed()).collect(Collectors.toList());
    }

    public void deleteSystemUserAuthenticationCache(SystemUserEntity entity, ResourceSourceEnum source) {
        createPrincipalAuthenticationTokenStream(entity, source).forEach(this::deleteSecurityUserDetailsCache);
    }

    /**
     * 删除系统用户的认证缓存
     *
     * @param token 认证 token
     */
    public void deleteSecurityUserDetailsCache(SimpleAuthenticationToken token) {
        ResourceSourceEnum resourceSource = ResourceSourceEnum.parse(token.getType());
        String key = resourceSource.getValue() + CacheProperties.DEFAULT_SEPARATOR + token.getPrincipal();

        String authenticationKey = springSecurityProperties.getAuthenticationCache().getName(key);
        redissonClient.getBucket(authenticationKey).deleteAsync();

        String authorizationKey = springSecurityProperties.getAuthorizationCache().getName(key);
        redissonClient.getBucket(authorizationKey).deleteAsync();

    }

    // -------------------------------- 资源管理 -------------------------------- //

    /**
     * 获取系统用户资源
     *
     * @param userDetails    spring 安全用户明细
     * @param type           资源类型
     * @param sourceContains 资源来源
     * @return 系统用户资源集合
     */
    public List<ResourceMeta> getSystemUserResource(SecurityUserDetails userDetails,
                                                    ResourceType type,
                                                    List<ResourceSourceEnum> sourceContains) {

        SystemUserEntity user = getSystemUser(userDetails.toBasicUserDetails());

        List<ResourceMeta> userResource = getSystemUserResource(user);

        Map<String, List<String>> ignoreResourceMap = pluginResourceService.getApplicationConfig().getIgnorePrincipalResource();

        if (MapUtils.isNotEmpty(ignoreResourceMap)) {
            List<ResourceMeta> removeList = new LinkedList<>();
            for (Map.Entry<String, List<String>> entry : ignoreResourceMap.entrySet()) {
                userResource
                        .stream()
                        .filter(r -> entry.getKey().equals(r.getApplicationName()))
                        .filter(r -> entry.getValue().contains(r.getCode()))
                        .forEach(removeList::add);
            }
            userResource.removeAll(removeList);
        }

        Stream<ResourceMeta> stream = userResource
                .stream()
                .filter(r -> r.getSources().stream().anyMatch(sourceContains::contains));
        List<ResourceType> defaultType = new LinkedList<>(ResourceType.DEFAULT_TYPE);
        if (Objects.nonNull(type)) {
            defaultType.add(type);
        }

        return stream.filter(r -> defaultType.contains(r.getType())).collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ResourceMeta::getId))), ArrayList::new));
    }

    /**
     * 设置系统用户权限信息
     *
     * @param user        系统用户
     * @param userDetails 当前的安全用户明细
     */
    public void setSystemUserAuthorities(SystemUserEntity user, SecurityUserDetails userDetails) {

        List<IdRoleAuthorityMeta> roleAuthorities = user
                .getGroupsInfo()
                .stream().filter(g -> DisabledOrEnabled.Enabled.equals(g.getStatus()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(roleAuthorities)) {
            userDetails.getRoleAuthorities().addAll(roleAuthorities);
        }

        // 构造用户的组资源
        List<ResourceMeta> userResource = getSystemUserResource(user);
        if (CollectionUtils.isNotEmpty(userResource)) {
            // 构造对应 spring security 的资源内容
            List<ResourceAuthority> resourceAuthorities = userResource
                    .stream()
                    .flatMap(this::createResourceAuthoritiesStream)
                    .toList();

            userDetails.getResourceAuthorities().addAll(resourceAuthorities);
        }

    }

    /**
     * 获取系统用户资源
     *
     * @param user 系统用户
     * @return 系统用户资源
     */
    public List<ResourceMeta> getSystemUserResource(SystemUserEntity user) {
        List<IdRoleAuthorityMeta> roleAuthorities = user.getGroupsInfo();

        List<ResourceMeta> userResource = new LinkedList<>();

        if (CollectionUtils.isEmpty(roleAuthorities)) {
            return userResource;
        }

        // 通过 id 获取组信息
        List<Integer> groupIds = roleAuthorities
                .stream()
                .map(IdRoleAuthorityMeta::getId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(groupIds)) {
            return userResource;
        }

        List<GroupEntity> groups = groupService.get(groupIds);

        // 获取组来源，用于过滤组的资源里有存在不同的资源来源细腻些
        List<ResourceSourceEnum> groupSources = groups
                .stream()
                .flatMap(g -> g.getSources().stream())
                .distinct()
                .collect(Collectors.toList());

        // 构造用户的组资源
        groups
                .stream()
                .flatMap(g -> getResourcesStream(g.getResourceMap(), groupSources))
                .forEach(userResource::add);

        // 构造用户的独立资源
        userResource.addAll(getResourcesStream(user.getResourceMap(), groupSources).distinct().toList());

        return userResource;
    }

    private Stream<ResourceMeta> getResourcesStream(Map<String, List<String>> resourceMap, List<ResourceSourceEnum> sources) {

        if (MapUtils.isEmpty(resourceMap)) {
            return Stream.empty();
        }

        List<ResourceMeta> result = new LinkedList<>();

        for (Map.Entry<String, List<String>> entry : resourceMap.entrySet()) {
            List<ResourceMeta> resources = getResources(entry.getKey());

            List<ResourceMeta> findResources = resources
                    .stream()
                    .filter(r -> entry.getValue().contains(r.getId()))
                    .filter(r -> r.getSources().stream().anyMatch(sources::contains))
                    .toList();

            result.addAll(findResources);
        }

        return result.stream().distinct();
    }

    private Stream<ResourceAuthority> createResourceAuthoritiesStream(ResourceMeta resource) {
        if (StringUtils.isBlank(resource.getAuthority())) {
            return Stream.empty();
        }

        String[] permissions = StringUtils.substringsBetween(
                resource.getAuthority(),
                ResourceAuthority.DEFAULT_RESOURCE_PREFIX,
                ResourceAuthority.DEFAULT_RESOURCE_SUFFIX
        );

        if (ArrayUtils.isEmpty(permissions)) {
            return Stream.empty();
        }

        return Arrays
                .stream(permissions)
                .map(ResourceAuthority::getPermissionValue)
                .map(p -> new ResourceAuthority(p, resource.getName(), resource.getValue()));
    }

    public void syncSystemUserGroup(List<GroupEntity> groups,
                                    SystemUserGroupSyncResolver systemUserGroupSyncResolver) {
        List<ResourceSourceEnum> sources = groups
                .stream()
                .flatMap(g -> g.getSources().stream())
                .collect(Collectors.toList());

        List<Integer> groupIds = groups.stream().map(IdEntity::getId).collect(Collectors.toList());

        for (ResourceSourceEnum source : sources) {
            Optional<SystemUserResolver> optional = systemUserResolvers
                    .stream()
                    .filter(r -> r.isSupport(source.getValue()))
                    .findFirst();

            if (optional.isEmpty()) {
                continue;
            }

            SystemUserResolver resolver = optional.get();
            List<SystemUserEntity> systemUsers = resolver.getUserByGroupId(groupIds);
            if (CollectionUtils.isEmpty(systemUsers)) {
                continue;
            }

            systemUsers
                    .stream()
                    .peek(sue -> systemUserGroupSyncResolver.syncSystemUserGroup(sue, groups))
                    .forEach(resolver::update);

        }

        deleteAuthorizationCache(sources);
    }

    public void updateSystemUser(SystemUserEntity user, String userType) {
        SystemUserResolver systemUserResolver = getSystemUserResolver(userType);
        systemUserResolver.update(user);
    }
}
