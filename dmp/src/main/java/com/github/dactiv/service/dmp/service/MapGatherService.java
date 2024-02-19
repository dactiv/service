package com.github.dactiv.service.dmp.service;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Concurrent;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.service.dmp.config.ApplicationConfig;
import com.github.dactiv.service.dmp.domain.meta.MapConfigPrepareMeta;
import com.github.dactiv.service.dmp.domain.meta.MapDataMeta;
import com.github.dactiv.service.dmp.enumerate.GatherTypeEnum;
import com.github.dactiv.service.dmp.resolver.MapResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.DigestUtils;

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 地图采集舒服
 *
 * @author maurice.chen
 */
@Service
@RequiredArgsConstructor
public class MapGatherService {

    private final AmqpTemplate amqpTemplate;

    private final ApplicationConfig applicationConfig;

    private final RedissonClient redissonClient;

    private final List<MapResolver> mapResolvers;

    public Page<MapDataMeta> mapSearch(TypeUserDetails<Integer> userDetails,
                                       GatherTypeEnum type,
                                       String searchId,
                                       PageRequest pageRequest,
                                       String keyword,
                                       List<String> city) {

        MapResolver mapResolver = mapResolvers.stream().filter(m -> m.getType().equals(type)).findFirst().orElseThrow(() -> new SystemException("找不到类型为 [" + type + "] 的地图解析器实现"));
        return mapResolver.search(userDetails, searchId, pageRequest, keyword, city);
    }

    @Concurrent("dacitv:saas:gather:map:get-search-id:[#userDetails.userId]")
    public String generateSearchId(TypeUserDetails<Integer> userDetails, List<GatherTypeEnum> mapTypes) {
        String id = this.getClass().getName() + Casts.DOT + userDetails.toUniqueValue() + System.currentTimeMillis();
        String result = DigestUtils.md5DigestAsHex(id.getBytes(Charset.defaultCharset()));
        mapResolvers
                .stream()
                .filter(m -> mapTypes.contains(m.getType()))
                .forEach(m -> m.createBySearchId(userDetails, result));
        return result;
    }

    public List<MapConfigPrepareMeta> getMapDetails() {
        return mapResolvers
                .stream()
                .map(MapResolver::getDetails)
                .filter(MapConfigPrepareMeta::isEnable)
                .toList();
    }

    public InputStream getMapIcon(String name) throws FileNotFoundException {
        String path = StringUtils.appendIfMissing(applicationConfig.getMapIconPath(), AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        return new FileInputStream(path + name);
    }

    public List<MapDataMeta> getMapDataMetas(TypeUserDetails<Integer> userDetails, String searchId, Map<String, List<String>> dataIds) {
        Map<String, List<MapDataMeta>> searchData =  mapResolvers
                .stream()
                .filter(m -> m.getDetails().isEnable())
                .flatMap(m -> m.getUserMapDataMetas(userDetails, searchId).stream())
                .collect(Collectors.groupingBy(d -> d.getGatherType().getValue()));

        List<MapDataMeta> dataMetas = new LinkedList<>();
        for (Map.Entry<String, List<MapDataMeta>> entry : searchData.entrySet()) {
            List<String> ids = dataIds.get(entry.getKey());
            entry.getValue().stream().filter(e -> ids.contains(e.getDataId())).forEach(dataMetas::add);
        }

        return dataMetas;
    }

    public Map<String, List<MapDataMeta>> getSearchData(TypeUserDetails<Integer> userDetails, String searchId) {

        return mapResolvers
                .stream()
                .flatMap(m -> m.getUserMapDataMetas(userDetails, searchId).stream())
                .collect(Collectors.groupingBy(d -> d.getGatherType().getValue()));
    }

    public Point2D.Double getAlibabaMapPoint(String address, String city) {
        AlibabaMapService alibabaMapService = mapResolvers
                .stream()
                .filter(m -> AlibabaMapService.class.isAssignableFrom(m.getClass()))
                .filter(m -> m.getDetails().isEnable())
                .map(m -> Casts.cast(m, AlibabaMapService.class))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到高德地图实现类，也许该类已禁用"));

        return alibabaMapService.getPoint(address, city);
    }
}
