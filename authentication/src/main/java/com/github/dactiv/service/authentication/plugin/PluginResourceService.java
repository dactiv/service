package com.github.dactiv.service.authentication.plugin;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.Time;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.nacos.event.NacosInstancesChangeEvent;
import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.framework.nacos.event.NacosServiceSubscribeEvent;
import com.github.dactiv.framework.nacos.event.NacosSpringEventManager;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.PluginInfo;
import com.github.dactiv.framework.spring.security.plugin.PluginEndpoint;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.service.authentication.config.ApplicationConfig;
import com.github.dactiv.service.authentication.domain.meta.ResourceMeta;
import com.github.dactiv.service.authentication.service.AuthorizationService;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 插件資源管理
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginResourceService {

    /**
     * 默认获取应用信息的后缀 uri
     */
    private static final String DEFAULT_PLUGIN_INFO_URL = "/actuator/plugin";

    private final RestTemplate restTemplate;

    private final NacosSpringEventManager nacosSpringEventManager;

    private final AuthorizationService authorizationService;

    private final List<PluginResourceInterceptor> pluginResourceInterceptor;

    private final PluginServiceValidator pluginServiceValidator;

    @Getter
    private final ApplicationConfig applicationConfig;

    /**
     * 服务实例缓存，用于记录当前的插件信息是否需要更新资源
     */
    private final Map<String, List<PluginInstance>> instanceCache = new LinkedHashMap<>();

    /**
     * 当前插件的所有资源集合
     */
    private final List<ResourceMeta> resources = new LinkedList<>();

    /**
     * 获取实例 info
     *
     * @param instance 实例
     * @return 实例信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInstanceInfo(Instance instance) {

        String http = StringUtils.prependIfMissing(instance.toInetAddr(), SpringMvcUtils.HTTP_PROTOCOL_PREFIX);
        String url = StringUtils.appendIfMissing(http, DEFAULT_PLUGIN_INFO_URL);

        return restTemplate.getForObject(url, Map.class);
    }

    /**
     * 匹配最大版本实例
     *
     * @param target 目标实例
     * @param source 原实例
     * @return 0 相等，小于0 小于，大于0 大于
     */
    public int comparingInstanceVersion(Instance target, Instance source) {
        return getInstanceVersion(target).compareTo(getInstanceVersion(source));
    }

    /**
     * 获取实例的版本信息
     *
     * @param instance 实例
     * @return 版本信息
     */
    public Version getInstanceVersion(Instance instance) {

        String version = instance.getMetadata().get(PluginInfo.DEFAULT_VERSION_NAME);
        String groupId = instance.getMetadata().get(PluginInfo.DEFAULT_GROUP_ID_NAME);
        String artifactId = instance.getMetadata().get(PluginInfo.DEFAULT_ARTIFACT_ID_NAME);

        return VersionUtil.parseVersion(version, groupId, artifactId);
    }

    @Concurrent(
            value = "dactiv:service:authentication:sync:plugin:resource:[#groupName]:[#serviceName]",
            exception = "同步插件信息遇到并发，不执行重试操作",
            waitTime = @Time(0L)
    )
    public void syncPluginResource(String groupName, String serviceName, List<Instance> instances) {

        if (applicationConfig.getIgnorePluginService().contains(serviceName)) {
            return;
        }

        Optional<Instance> optional = instances.stream().max(this::comparingInstanceVersion);

        if (optional.isEmpty()) {
            log.warn("找不到服务为 [" + groupName + "][" + serviceName + "] 的最多版本实例");
            return;
        }

        Instance instance = optional.get();
        // 获取实例版本信息
        Version version = getInstanceVersion(instance);

        PluginInstance pluginInstance = Casts.of(instance, PluginInstance.class);
        pluginInstance.setServiceName(serviceName);
        pluginInstance.setVersion(version);
        pluginInstance.setGroup(groupName);

        List<PluginInstance> cache = instanceCache.computeIfAbsent(groupName, k -> new LinkedList<>());

        Optional<PluginInstance> exist = cache
                .stream()
                .filter(c -> c.getServiceName().equals(pluginInstance.getServiceName()))
                .findFirst();
        // 判断一下当前缓存是否存在同样的实例，如果存在，判断缓存的实例版本和当前的实例版本，如果当前实例版本较大，在覆盖一次資源内容
        if (exist.isPresent()) {

            PluginInstance existData = exist.get();

            if (existData.getVersion().compareTo(pluginInstance.getVersion()) > 0) {
                return;
            }

            cache.remove(existData);
        }

        Map<String, Object> info = getInstanceInfo(instance);
        pluginInstance.setInfo(info);

        cache.add(pluginInstance);

        enabledApplicationResource(pluginInstance);

    }

    /**
     * 启用应用资源
     *
     * @param instance 插件实例
     */
    public void enabledApplicationResource(PluginInstance instance) {

        if (Objects.isNull(instance) || Objects.isNull(instance.getVersion())) {
            return;
        }

        // 应用名称
        String applicationName = instance.getServiceName();

        if (log.isDebugEnabled()) {
            log.debug("开始绑定组为 [" + instance.getGroup() + "] 的 [" + applicationName + " " + instance.getVersion() + "] 应用资源信息");
        }

        List<PluginInfo> pluginList = createPluginInfoListFromInfo(instance.getInfo());
        // 启用資源得到新的資源集合
        List<ResourceMeta> newResourceList = pluginList
                .stream()
                .map(p -> createResource(p, instance, null))
                .collect(Collectors.toList());

        List<ResourceMeta> unmergeResourceList = TreeUtils.unBuildGenericTree(newResourceList);

        resources.removeIf(r -> r.getApplicationName().equals(instance.getServiceName()));
        resources.addAll(unmergeResourceList);

        if (log.isDebugEnabled()) {
            log.debug("绑定组为 [" + instance.getGroup() + "] 的 [" + applicationName + " " + instance.getVersion() + "] 应用资源信息完成");
        }

        if (CollectionUtils.isNotEmpty(pluginResourceInterceptor)) {
            pluginResourceInterceptor.forEach(i -> i.postSyncPlugin(instance, unmergeResourceList));
        }
        authorizationService.setResourceMetas(resources);
    }

    /**
     * 通过 info 信息创建插件信息实体集合
     *
     * @param info info 信息
     * @return 插件信息实体集合
     */
    private List<PluginInfo> createPluginInfoListFromInfo(Map<String, Object> info) {

        List<PluginInfo> result = new LinkedList<>();

        List<Map<String, Object>> pluginMapList = Casts.cast(info.get(PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME));

        for (Map<String, Object> pluginMap : pluginMapList) {
            PluginInfo pluginInfo = createPluginInfo(pluginMap);
            result.add(pluginInfo);
        }

        return result;
    }

    /**
     * 通过插件 map 创建插件信息实体
     *
     * @param pluginMap 插件 map
     * @return 插件信息实体
     */
    private PluginInfo createPluginInfo(Map<String, Object> pluginMap) {

        List<Map<String, Object>> children = new LinkedList<>();

        if (pluginMap.containsKey(PluginInfo.DEFAULT_CHILDREN_NAME)) {
            children = Casts.cast(pluginMap.get(PluginInfo.DEFAULT_CHILDREN_NAME));
            pluginMap.remove(PluginInfo.DEFAULT_CHILDREN_NAME);
        }

        PluginInfo pluginInfo = Casts.convertValue(pluginMap, PluginInfo.class);

        List<Tree<String, PluginInfo>> childrenNode = new LinkedList<>();

        pluginInfo.setChildren(childrenNode);

        for (Map<String, Object> child : children) {
            PluginInfo childNode = createPluginInfo(child);
            childrenNode.add(childNode);
        }

        return pluginInfo;
    }

    /**
     * 创建資源
     *
     * @param plugin   插件信息
     * @param instance 服务信息
     * @param parent   夫类资源
     * @return 新的資源
     */
    private ResourceMeta createResource(PluginInfo plugin, PluginInstance instance, ResourceMeta parent) {
        ResourceMeta target = Casts.of(
                plugin,
                ResourceMeta.class,
                IdEntity.ID_FIELD_NAME,
                PluginInfo.DEFAULT_CHILDREN_NAME,
                PluginInfo.DEFAULT_SOURCES_NAME
        );

        Assert.notEmpty(plugin.getSources(), "插件实体 [" + Casts.convertValue(plugin, Map.class) + "] 的 sources 为空");

        List<ResourceSourceEnum> sources = plugin
                .getSources()
                .stream()
                .map(s -> NameEnumUtils.parse(s, ResourceSourceEnum.class))
                .collect(Collectors.toList());

        target.setSources(sources);
        target.setType(NameEnumUtils.parse(plugin.getType(), ResourceType.class));

        if (StringUtils.equals(plugin.getParent(), PluginInfo.DEFAULT_ROOT_PARENT_NAME)) {
            target.setParentId(null);
        } else if (Objects.nonNull(parent)) {
            target.setParentId(parent.getId());
        }

        if (StringUtils.isBlank(target.getApplicationName())) {
            target.setApplicationName(instance.getServiceName());
        }

        if (instance.getVersion() != null) {
            target.setVersion(instance.getVersion().toString());
        }

        target.setCode(plugin.getId());

        String id = generateId(target);
        target.setId(id);

        // 设置 target 变量的子节点
        plugin.getChildren()
                .stream()
                .map(c -> createResource(Casts.cast(c, PluginInfo.class), instance, target))
                .forEach(r -> target.getChildren().add(r));

        return target;
    }

    /**
     * 生成主键 id
     *
     * @param target 资源目标元数据
     * @return 主键 id
     */
    private String generateId(ResourceMeta target) {
        String s = target.getApplicationName() + target.getCode() + target.getType() + target.getSources() + target.getParent();
        return DigestUtils.md5DigestAsHex(s.getBytes(Charset.defaultCharset()));
    }

    /**
     * 禁用资源
     *
     * @param nacosService nacos 服务信息
     */
    public void disabledApplicationResource(NacosService nacosService) {

        List<ResourceSourceEnum> sources = resources
                .stream()
                .filter(r -> r.getApplicationName().equals(nacosService.getName()))
                .flatMap(r -> r.getSources().stream())
                .distinct()
                .collect(Collectors.toList());

        resources.removeIf(r -> r.getApplicationName().equals(nacosService.getName()));
        // 查询所有符合条件的资源,并设置为禁用状态
        authorizationService.deleteAuthorizationCache(sources);
        // 清除组的实例缓存
        List<PluginInstance> instances = instanceCache.computeIfAbsent(nacosService.getGroupName(), k -> new LinkedList<>());
        instances.removeIf(p -> p.getServiceName().equals(nacosService.getName()));
        if (CollectionUtils.isNotEmpty(pluginResourceInterceptor)) {
            pluginResourceInterceptor.forEach(i -> i.postDisabledApplicationResource(nacosService));
        }

        authorizationService.setResourceMetas(resources);
    }

    /**
     * 监听 nacos 服务被订阅事件，自动同步插件資源
     *
     * @param event 事件原型
     */
    @EventListener
    public void onNacosServiceSubscribeEvent(NacosServiceSubscribeEvent event) {
        NacosService nacosService = Casts.cast(event.getSource());
        syncPluginResource(nacosService.getGroupName(), nacosService.getName(), nacosService.getInstances());
    }

    /**
     * 监听 nasoc 服务变化事件
     *
     * @param event 事件原型
     */
    @EventListener
    public void onNacosInstancesChangeEvent(NacosInstancesChangeEvent event) {
        NacosService nacosService = Casts.cast(event.getSource());

        if (CollectionUtils.isEmpty(nacosService.getInstances())) {
            disabledApplicationResource(nacosService);
            return;
        }

        syncPluginResource(nacosService.getGroupName(), nacosService.getName(), nacosService.getInstances());
    }

    /**
     * 重新订阅所有服务
     */
    @Concurrent(value = "dactiv:service:authentication:subscribe_or_unsubscribe:plugin", exception = "正在执行，请稍后在试。。", waitTime = @Time(0L))
    public void resubscribeAllService() {
        nacosSpringEventManager.expiredAllListener();
        nacosSpringEventManager.scanThenUnsubscribeService();

        pluginServiceValidator.clearExceptionServices();

        nacosSpringEventManager.scanThenSubscribeService();
    }

    /**
     * 获取资源集合
     *
     * @return 资源集合
     */
    public List<ResourceMeta> getResources() {
        return this
                .resources
                .stream()
                .map(r -> Casts.of(r, ResourceMeta.class, PluginInfo.DEFAULT_CHILDREN_NAME))
                .collect(Collectors.toList());
    }

    /**
     * 获取插件服务名称集合
     *
     * @return 插件服务名称集合
     */
    public Set<String> getPluginServerNames() {
        return getResources().stream().collect(Collectors.groupingBy(ResourceMeta::getApplicationName)).keySet();
    }
}
