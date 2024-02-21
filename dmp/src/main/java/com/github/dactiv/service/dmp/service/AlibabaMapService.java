package com.github.dactiv.service.dmp.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.page.TotalPage;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.domain.meta.CloudSecurityMeta;
import com.github.dactiv.service.dmp.config.AlibabaMapConfig;
import com.github.dactiv.service.dmp.domain.meta.MapConfigPrepareMeta;
import com.github.dactiv.service.dmp.domain.meta.MapDataMeta;
import com.github.dactiv.service.dmp.enumerate.GatherTypeEnum;
import com.github.dactiv.service.dmp.resolver.support.RedissonMapResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.awt.geom.Point2D;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 高德地图服务
 *
 * @author maurice.chen
 */
@Service
@RequiredArgsConstructor
public class AlibabaMapService extends RedissonMapResolver {

    public static final String QUERY_DATA_URL = "https://restapi.amap.com/v3/place/text";

    public static final String QUERY_POINT_URL = "https://restapi.amap.com/v3/geocode/geo";

    private final AlibabaMapConfig alibabaMapConfig;

    private final RestTemplate restTemplate;

    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    @Autowired
    public void setRedissonClient(RedissonClient redissonClient) {
        super.setRedissonClient(redissonClient);
    }

    private Map<String, Object> exchangeApi(String url, MultiValueMap<String, String> param, CloudSecurityMeta cloudSecurityMeta) {
        String paramString = Casts.castRequestBodyMapToString(param);
        String sign = sign(param, cloudSecurityMeta);

        if (StringUtils.isNotEmpty(paramString)) {
            url = StringUtils.appendIfMissing(url, "?");
        }
        ResponseEntity<Map<String, Object>> entity = restTemplate.exchange(url + paramString + "&sig=" + sign, HttpMethod.GET, null, new ParameterizedTypeReference<>(){});

        Map<String, Object> body = checkBody(entity);

        if ("0".equals(body.get(SystemConstants.STATUS_TABLE_FLED_NAME).toString())) {
            throw new ServiceException(body.get("info").toString());
        }
        return body;
    }

    private CloudSecurityMeta getCloudSecurityMeta() {
        CloudSecurityMeta cloudSecurityMeta = alibabaMapConfig.getCloudSecurityMetas().get(index.getAndIncrement());

        if (index.get() >= alibabaMapConfig.getCloudSecurityMetas().size() - 1) {
            index.compareAndSet(index.get(), 0);
        }

        return cloudSecurityMeta;
    }

    private MapDataMeta createMapDataMeta(Map<String, Object> data) {
        MapDataMeta mapDataMeta = new MapDataMeta();

        mapDataMeta.setDataId(data.get("id").toString());
        mapDataMeta.setType(List.of(StringUtils.splitByWholeSeparator(data.get("type").toString(),Casts.SEMICOLON)));
        mapDataMeta.setName(data.get("name").toString());
        mapDataMeta.setAddress(data.get("address").toString());

        mapDataMeta.getRegionMeta().setProvince(data.get("pname").toString());
        mapDataMeta.getRegionMeta().setCity(data.get("cityname").toString());
        mapDataMeta.getRegionMeta().setDistrict(data.get("adname").toString());

        if (String.class.isAssignableFrom(data.get("tel").getClass())) {
            Arrays.stream(StringUtils.splitByWholeSeparator(data.get("tel").toString(), Casts.SEMICOLON)).forEach(s -> mapDataMeta.getContact().add(s));
        } else {
            //noinspection unchecked
            mapDataMeta.setContact(Casts.cast(data.get("tel"),ArrayList.class));
        }

        String[] location = StringUtils.splitByWholeSeparator(data.get("location").toString(), Casts.COMMA);
        mapDataMeta.setPoint(new Point2D.Double(Double.parseDouble(location[0]), Double.parseDouble(location[1])));
        mapDataMeta.setGatherType(GatherTypeEnum.ALIBABA_MAP);
        return mapDataMeta;
    }

    private String sign(MultiValueMap<String, String> param, CloudSecurityMeta cloudSecurityMeta) {
        // 获取 MultiValueMap 中的所有键
        List<String> keys = new ArrayList<>(param.keySet());

        // 对键进行升序排序
        Collections.sort(keys);

        // 创建一个新的 MultiValueMap，并按照升序排序后的键重新放入值
        MultiValueMap<String, String> sortedMultiValueMap = new LinkedMultiValueMap<>();
        for (String key : keys) {
            List<String> values = param.get(key);
            sortedMultiValueMap.put(key, values);
        }

        String paramString = Casts.castRequestBodyMapToString(sortedMultiValueMap);

        return DigestUtils.md5DigestAsHex((paramString + cloudSecurityMeta.getSecretKey()).getBytes(Charset.defaultCharset()));
    }

    private Map<String, Object> checkBody(ResponseEntity<Map<String, Object>> entity) {
        if (HttpStatus.OK.value() != entity.getStatusCode().value()) {
            throw new SystemException("调用高德地图接口出现错误:" + HttpStatus.valueOf(entity.getStatusCode().value()).getReasonPhrase());
        }

        return Objects.requireNonNull(entity.getBody(), "高德地图响应体中找不到数据");
    }


    @Override
    public MapConfigPrepareMeta getDetails() {
        return Casts.of(alibabaMapConfig, MapConfigPrepareMeta.class);
    }

    @Override
    public GatherTypeEnum getType() {
        return GatherTypeEnum.ALIBABA_MAP;
    }


    @Override
    protected Page<MapDataMeta> doSearch(PageRequest request, String keyword, List<String> city) {

        CloudSecurityMeta cloudSecurityMeta = getCloudSecurityMeta();

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();

        param.add("key", cloudSecurityMeta.getSecretId());
        param.add("keywords", keyword);
        param.add("city", city.getLast());
        param.add("citylimit", String.valueOf(alibabaMapConfig.isCityLimit()));
        param.add("page", String.valueOf(request.getNumber()));
        param.add("offset", String.valueOf(request.getSize()));

        Map<String, Object> body = exchangeApi(QUERY_DATA_URL, param, cloudSecurityMeta);

        Long total = Casts.cast(body.get("count"), Long.class);

        List<Map<String, Object>> data = Casts.cast(body.get("pois"));

        if (CollectionUtils.isEmpty(data)) {
            data = new ArrayList<>();
        }

        List<MapDataMeta> elements = data
                .stream()
                .map(this::createMapDataMeta)
                .collect(Collectors.toList());
        return new TotalPage<>(request, elements, total);
    }

    @Override
    protected CacheProperties getMapDataCache() {
        return alibabaMapConfig.getDataCache();
    }

    @Override
    protected CacheProperties getUserSearchCache() {
        return alibabaMapConfig.getSearchCache();
    }

    public Point2D.Double getPoint(String address, String city) {

        CloudSecurityMeta cloudSecurityMeta = getCloudSecurityMeta();

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();

        param.add("key", cloudSecurityMeta.getSecretId());
        param.add("address", address);

        if (StringUtils.isNotEmpty(city)) {
            param.add("city", city);
        }

        Map<String, Object> body = exchangeApi(QUERY_POINT_URL, param, cloudSecurityMeta);
        List<Map<String, Object>> geocodes = Casts.cast(body.get("geocodes"));
        if (CollectionUtils.isNotEmpty(geocodes)) {
            Map<String, Object> geoData = geocodes.getFirst();
            String[] location = StringUtils.splitByWholeSeparator(geoData.get("location").toString(), Casts.COMMA);
            return new Point2D.Double(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
        }
        return null;
    }
}
