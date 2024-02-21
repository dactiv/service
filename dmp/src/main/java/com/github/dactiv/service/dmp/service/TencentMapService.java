package com.github.dactiv.service.dmp.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.page.TotalPage;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.dmp.config.TencentMapConfig;
import com.github.dactiv.service.dmp.domain.meta.MapConfigPrepareMeta;
import com.github.dactiv.service.dmp.domain.meta.MapDataMeta;
import com.github.dactiv.service.dmp.enumerate.GatherTypeEnum;
import com.github.dactiv.service.dmp.resolver.support.RedissonMapResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.awt.geom.Point2D;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 腾讯地图服务
 *
 * @author marucie.chen
 */
@Service
@RequiredArgsConstructor
public class TencentMapService extends RedissonMapResolver {

    public static final String QUERY_DATA_URL = "https://apis.map.qq.com/ws/place/v1/search";

    private final TencentMapConfig tencentMapConfig;

    private final RestTemplate restTemplate;

    private final AtomicInteger index = new AtomicInteger(0);



    private String getKey() {
        String key = tencentMapConfig.getKeys().get(index.getAndIncrement());
        if (index.get() >= tencentMapConfig.getKeys().size() - 1) {
            index.compareAndSet(index.get(), 0);
        }

        return key;
    }

    private MapDataMeta createMapDataMeta(Map<String, Object> data) {
        MapDataMeta mapDataMeta = new MapDataMeta();

        mapDataMeta.setDataId(data.get("id").toString());
        mapDataMeta.setType(List.of(StringUtils.splitByWholeSeparator(data.get("category").toString(), CacheProperties.DEFAULT_SEPARATOR)));
        mapDataMeta.setName(data.get("title").toString());
        mapDataMeta.setAddress(data.get("address").toString());

        //noinspection unchecked
        Map<String, Object> adInfo = Casts.cast(data.get("ad_info"), Map.class);

        mapDataMeta.getRegionMeta().setProvince(adInfo.get("province").toString());
        mapDataMeta.getRegionMeta().setCity(adInfo.get("city").toString());
        mapDataMeta.getRegionMeta().setDistrict(adInfo.get("district").toString());

        if (String.class.isAssignableFrom(data.get("tel").getClass())) {
            Arrays.stream(StringUtils.splitByWholeSeparator(data.get("tel").toString(), Casts.COMMA)).forEach(s -> mapDataMeta.getContact().add(s));
        } else {
            //noinspection unchecked
            mapDataMeta.setContact(Casts.cast(data.get("tel"),ArrayList.class));
        }
        //noinspection unchecked
        Map<String, Object> location = Casts.cast(data.get("location"), Map.class);
        mapDataMeta.setPoint(new Point2D.Double(Double.parseDouble(location.get("lat").toString()), Double.parseDouble(location.get("lng").toString())));

        mapDataMeta.setGatherType(GatherTypeEnum.TENCENT_MAP);
        return mapDataMeta;
    }

    private Map<String, Object> exchangeApi(String url, MultiValueMap<String, String> param) {
        String paramString = Casts.castRequestBodyMapToString(param);
        if (StringUtils.isNotEmpty(paramString)) {
            url = StringUtils.appendIfMissing(url, "?");
        }
        ResponseEntity<Map<String, Object>> entity = restTemplate.exchange(url + paramString, HttpMethod.GET, null, new ParameterizedTypeReference<>(){});

        Map<String, Object> body = checkBody(entity);

        if (!"0".equals(body.get(SystemConstants.STATUS_TABLE_FLED_NAME).toString())) {
            throw new ServiceException(body.get(RestResult.DEFAULT_MESSAGE_NAME).toString());
        }
        return body;
    }

    private Map<String, Object> checkBody(ResponseEntity<Map<String, Object>> entity) {
        if (HttpStatus.OK.value() != entity.getStatusCode().value()) {
            throw new SystemException("调用腾讯地图接口出现错误:" + HttpStatus.valueOf(entity.getStatusCode().value()).getReasonPhrase());
        }

        return Objects.requireNonNull(entity.getBody(), "腾讯地图响应体中找不到数据");
    }


    @Override
    public MapConfigPrepareMeta getDetails() {
        return Casts.of(tencentMapConfig, MapConfigPrepareMeta.class);
    }

    @Override
    public GatherTypeEnum getType() {
        return GatherTypeEnum.TENCENT_MAP;
    }

    @Override
    protected Page<MapDataMeta> doSearch(PageRequest request, String keyword, List<String> city) {

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();

        param.add("key", getKey());
        param.add("keyword", URLEncoder.encode(keyword, Charset.defaultCharset()));
        param.add("boundary", "region(" + city.getLast() +")");
        param.add("page_size", String.valueOf(request.getSize()));
        param.add("page_index", String.valueOf(request.getNumber()));

        Map<String, Object> body = exchangeApi(QUERY_DATA_URL, param);

        Long total = Casts.cast(body.get("count"), Long.class);

        List<Map<String, Object>> data = Casts.cast(body.get("data"));

        if (CollectionUtils.isEmpty(data)) {
            data = new ArrayList<>();
        }

        List<MapDataMeta> elements = data
                .stream()
                .map(this::createMapDataMeta)
                //.map(m -> Casts.of(m, MapDataResponseBody.class))
                .collect(Collectors.toList());

        return new TotalPage<>(request, elements, total);
    }

    @Override
    protected CacheProperties getMapDataCache() {
        return tencentMapConfig.getDataCache();
    }

    @Override
    protected CacheProperties getUserSearchCache() {
        return tencentMapConfig.getSearchCache();
    }
}
