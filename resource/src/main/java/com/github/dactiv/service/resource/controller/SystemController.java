package com.github.dactiv.service.resource.controller;


import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.domain.meta.IdValueMeta;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import com.github.dactiv.service.resource.config.ApplicationConfig;
import com.github.dactiv.service.resource.domain.meta.DataDictionaryMeta;
import com.github.dactiv.service.resource.service.EnumerateResourceService;
import com.github.dactiv.service.resource.service.dictionary.DictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 配置管理控制器
 *
 * @author maurice.chen
 */
@Slf4j
@RefreshScope
@RestController
@RequiredArgsConstructor
public class SystemController {

    public static final String DEFAULT_EVN_URI = "actuator/env";

    private final DictionaryService dictionaryService;

    private final EnumerateResourceService enumerateResourceService;

    private final ApplicationConfig applicationConfig;

    private final DiscoveryClient discoveryClient;

    private final RestTemplate restTemplate;

    /**
     * 根据字典类型查询数据字典数据并分组
     *
     * @param typeIds 字典类型 id 集合
     * @return key 为 typeId value 为数据字典集合的 map
     */
    @GetMapping("findGroupDataDictionariesByTypeId")
    public Map<Integer, List<DataDictionaryMeta>> findGroupDataDictionariesByTypeId(@RequestParam List<Integer> typeIds) {
        Map<Integer, List<DataDictionaryMeta>> group = new LinkedHashMap<>();

        for (Integer typeId : typeIds) {
            group.put(typeId, dictionaryService.findDataDictionaries(typeId));
        }

        return group;
    }

    /**
     * 根据字典类型查询数据字典
     *
     * @param typeId 字典类型 id
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionariesByTypeId")
    public List<DataDictionaryMeta> findDataDictionariesByType(@RequestParam Integer typeId) {
        return dictionaryService.findDataDictionaries(typeId);
    }

    /**
     * 获取数据字典
     *
     * @param code 数据字典代码
     * @return 数据字典集合
     */
    @GetMapping("getDataDictionary/{code}")
    public DataDictionaryMeta getDataDictionary(@PathVariable String code) {
        return dictionaryService
                .getDataDictionaryService()
                .getByCode(code);

    }

    /**
     * 获取数据字典
     *
     * @param code 数据字典代码
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionaries/{code}")
    public List<DataDictionaryMeta> findDataDictionaries(@PathVariable String code) {
        return dictionaryService
                .getDataDictionaryService()
                .findDataDictionaryMetas(code);

    }

    /**
     * 获取服务枚举
     *
     * @param service       服务名
     * @param enumerateName 枚举名
     * @return 枚举信息
     */
    @GetMapping("getServiceEnumerate")
    public Object getServiceEnumerate(@RequestParam String service,
                                      @RequestParam String enumerateName,
                                      @RequestParam(required = false, defaultValue = "false") boolean idValueFormat,
                                      @RequestParam(required = false) List<String> ignoreValue) {
        Map<String, Object> enumerate = enumerateResourceService.getServiceEnumerate(service, enumerateName, ignoreValue);
        return IdValueMeta.ofMap(enumerate, idValueFormat);
    }

    /**
     * 批量获取服务枚举
     *
     * @param map key 为 service 值，value 为 enumerateName
     * @return 服务枚举名称 为 key，对应的枚举集合为 value
     */
    @PostMapping("getServiceEnumerates")
    public Map<String, Map<String, Object>> getServiceEnumerates(@RequestBody Map<String, List<IdValueMeta<String, List<String>>>> map,
                                                                 @RequestParam(required = false, defaultValue = "false") boolean idValueFormat) {

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<IdValueMeta<String, List<String>>>> entry : map.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> valueMap = new LinkedHashMap<>();
            for (IdValueMeta<String, List<String>> value : entry.getValue()) {
                Map<String, Object> enumerate = enumerateResourceService.getServiceEnumerate(key, value.getId(), value.getValue());
                valueMap.put(value.getId(), IdValueMeta.ofMap(enumerate, idValueFormat));
            }
            result.put(key, valueMap);
        }

        return result;

    }

    /**
     * 同步所有枚举
     *
     * @return 所有服务枚举信息
     */
    @PostMapping("syncEnumerate")
    @Idempotent(key = "config:sync-enumerate")
    @PreAuthorize("hasAuthority('perms[enumerate:sync]')")
    @Plugin(name = "同步所有枚举", parent = "enumerate", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    public RestResult<Map<String, Map<String, Map<String, Object>>>> syncEnumerate() {
        enumerateResourceService.syncEnumerate();
        return RestResult.ofSuccess("同步系统枚举成功", enumerateResourceService.getServiceEnumerate());

    }

    /**
     * 获取服务枚举
     *
     * @return 服务枚举信息
     */
    @GetMapping("enumerate")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "系统枚举查询", id = "enumerate", parent = "basic", icon = "icon-scheme", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Map<String, Map<String, Map<String, Object>>> enumerate() {
        return enumerateResourceService.getServiceEnumerate();
    }

    /**
     * 获取服务枚举
     *
     * @return 服务枚举信息
     */
    @GetMapping("environment")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "环境变量查询", id = "environment", parent = "basic", icon = "icon-adjust", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Map<String, Object> environment() {

        Map<String, Object> result = new LinkedHashMap<>();

        List<String> services = discoveryClient.getServices();

        services.forEach(s -> {
            List<ServiceInstance> instances = discoveryClient.getInstances(s);

            ServiceInstance instance = instances.get(RandomSource.XO_RO_SHI_RO_128_PP.create().nextInt(0, instances.size()));

            if (Objects.nonNull(instance)) {
                String url = instance.getUri() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + DEFAULT_EVN_URI;
                try {
                    ResponseEntity<Map<String, Object>> res = restTemplate.exchange(url, HttpMethod.GET, null, SystemConstants.MAP_REFERENCE);
                    Map<String, Object> data = res.getBody();
                    result.put(s, data);
                } catch (Exception e) {
                    log.warn("获取 [" + s + "] 服务环境变量出错", e);
                }
            }
        });

        return result;
    }

    /**
     * 获取环境变量值
     *
     * @param service 服务名称
     * @param key     键
     * @return reset 结果集
     */
    @GetMapping("getEnvironment")
    @PreAuthorize("isAuthenticated()")
    public RestResult<Object> getEnvironment(@RequestParam String service, @RequestParam String key) {
        List<ServiceInstance> instances = discoveryClient.getInstances(service);
        Assert.isTrue(CollectionUtils.isNotEmpty(instances), "找不到服务为 [" + service + "] 的实例");

        ServiceInstance instance = instances.get(RandomSource.XO_RO_SHI_RO_128_PP.create().nextInt(0, instances.size()));
        Map<String, Object> data = new LinkedHashMap<>();

        if (Objects.nonNull(instance)) {
            String url = instance.getUri() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + DEFAULT_EVN_URI;
            try {
                //noinspection unchecked
                data = restTemplate.getForObject(url, Map.class);
            } catch (Exception e) {
                log.warn("获取 [" + service + "] 服务环境变量出错", e);
            }
        }

        if (MapUtils.isEmpty(data)) {
            return RestResult.ofSuccess(null);
        }

        for (Map.Entry<String, Object> d : data.entrySet()) {
            //noinspection unchecked
            List<Object> list = Casts.cast(d.getValue(), List.class);
            for (Object o : list) {
                Map<String, Object> map = Casts.cast(o);
                //noinspection unchecked
                Map<String, Object> properties = Casts.cast(map.get(StringLookupFactory.KEY_PROPERTIES), Map.class);
                if (MapUtils.isNotEmpty(properties) && properties.containsKey(key)) {
                    return RestResult.ofSuccess(properties.get(key));
                }
            }
        }

        return RestResult.ofSuccess(null);
    }

    /**
     * 获取环境变量值
     *
     * @param service 服务
     * @param key     健名称
     * @return 值
     */
    @GetMapping("getEnvironmentValue")
    @PreAuthorize("isAuthenticated()")
    public RestResult<Object> getEnvironmentValue(@RequestParam String service, @RequestParam String key) {
        List<ServiceInstance> instances = discoveryClient.getInstances(service);

        if (CollectionUtils.isEmpty(instances)) {
            return RestResult.ofSuccess(null);
        }

        ServiceInstance instance = instances.get(RandomSource.XO_RO_SHI_RO_128_PP.create().nextInt(0, instances.size()));
        if (Objects.isNull(instance)) {
            return RestResult.ofSuccess(null);
        }

        String url = instance.getUri() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + DEFAULT_EVN_URI;

        try {
            //noinspection unchecked
            Map<String, List<Map<String, Object>>> data = restTemplate.getForObject(url, Map.class);

            if (MapUtils.isEmpty(data)) {
                return RestResult.ofSuccess(null);
            }

            return RestResult.ofSuccess(getEnvironmentValue(data, key));

        } catch (Exception e) {
            log.warn("获取 [" + service + "] 服务环境变量出错", e);
        }

        return RestResult.ofSuccess(null);
    }

    private List<Map<String, Object>> getHasPropertiesValueEnvironment(List<Map<String, Object>> data) {
        List<Map<String, Object>> result = new LinkedList<>();

        for (Map<String, Object> map : data) {
            if (!map.containsKey("properties")) {
                continue;
            }
            Map<String, Object> properties = new LinkedHashMap<>(map);
            result.add(properties);
        }

        return result;
    }

    private Object getEnvironmentValue(Map<String, List<Map<String, Object>>> data, String key) {

        if (applicationConfig.getIgnoreEnvironmentStartWith().stream().anyMatch(key::startsWith)) {
            return null;
        }

        for (Map.Entry<String, List<Map<String, Object>>> entry : data.entrySet()) {
            List<Map<String, Object>> properties = Casts.cast(entry.getValue());
            List<Map<String, Object>> environmentList = getHasPropertiesValueEnvironment(properties);

            for (Map<String, Object> environment : environmentList) {
                //noinspection unchecked
                Map<String, Object> environmentProperties = Casts.cast(environment.get("properties"), Map.class);

                if (environmentProperties.containsKey(key)) {
                    Map<String, Object> valueMap = Casts.cast(environmentProperties.get(key));
                    return valueMap.get(Property.VALUE_FIELD);
                }
            }
        }

        return null;
    }

    /**
     * 获取环境变量值
     *
     * @param map key 为服务名称，value 为环境变量 key
     * @return 服务名为 key 值为环境变量名和对应的值
     */
    @GetMapping("getEnvironmentValues")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Map<String, Object>> getEnvironmentValues(@RequestBody Map<String, List<String>> map) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        Map<String, Map<String, List<Map<String, Object>>>> cache = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {

            Map<String, List<Map<String, Object>>> serviceEnvironment = cache.get(entry.getKey());

            if (MapUtils.isEmpty(serviceEnvironment)) {
                List<ServiceInstance> instances = discoveryClient.getInstances(entry.getKey());
                ServiceInstance instance = instances.get(RandomSource.XO_RO_SHI_RO_128_PP.create().nextInt(0, instances.size()));
                String url = instance.getUri() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + DEFAULT_EVN_URI;

                try {
                    //noinspection unchecked
                    Map<String, List<Map<String, Object>>> data = restTemplate.getForObject(url, Map.class);
                    cache.put(entry.getKey(), data);
                } catch (Exception e) {
                    log.warn("获取 [" + entry.getKey() + "] 服务环境变量出错", e);
                }
            }

            if (MapUtils.isEmpty(serviceEnvironment)) {
                continue;
            }

            Map<String, Object> valueMap = new LinkedHashMap<>();

            for (String key : entry.getValue()) {
                Object value = getEnvironmentValue(serviceEnvironment, key);
                if (Objects.isNull(value)) {
                    continue;
                }
                valueMap.put(key, value);
            }

            if (MapUtils.isNotEmpty(valueMap)) {
                result.put(entry.getKey(), valueMap);
            }

        }

        return result;
    }

}
