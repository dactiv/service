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
import com.github.dactiv.service.dmp.config.BaiduMapConfig;
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
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 百度地图服务
 *
 * @author maurice.chen
 */
@Service
@RequiredArgsConstructor
public class BaiduMapService extends RedissonMapResolver {
    public static final String API_DOMAIN = "https://api.map.baidu.com";

    public static final String QUERY_DATA_URL = API_DOMAIN + "/place/v2/search";
    private final BaiduMapConfig baiduMapConfig;

    private final RestTemplate restTemplate;

    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    @Autowired
    public void setRedissonClient(RedissonClient redissonClient) {
        super.setRedissonClient(redissonClient);
    }

    private String getKey() {
        String key = baiduMapConfig.getKeys().get(index.getAndIncrement());
        if (index.get() >= baiduMapConfig.getKeys().size() - 1) {
            index.compareAndSet(index.get(), 0);
        }

        return key;
    }

    private MapDataMeta createMapDataMeta(Map<String, Object> data) {
        MapDataMeta mapDataMeta = new MapDataMeta();

        mapDataMeta.setDataId(data.get("uid").toString());
        mapDataMeta.setName(data.get("name").toString());
        mapDataMeta.setAddress(data.get("address").toString());

        mapDataMeta.getRegionMeta().setProvince(data.get("province").toString());
        mapDataMeta.getRegionMeta().setCity(data.get("city").toString());
        mapDataMeta.getRegionMeta().setDistrict(data.get("area").toString());

        if (data.get("detail").toString().equals("1")) {
            //noinspection unchecked
            Map<String, Object> info = Casts.cast(data.get("detail_info"), Map.class);
            if (Objects.nonNull(info.get("tag"))) {
                String tag = info.get("tag").toString();
                mapDataMeta.setType(List.of(StringUtils.splitByWholeSeparator(tag,Casts.SEMICOLON)));
            }
        }

        if (data.containsKey("telephone")) {
            if (String.class.isAssignableFrom(data.get("telephone").getClass())) {
                Arrays.stream(StringUtils.splitByWholeSeparator(data.get("telephone").toString(), Casts.COMMA)).forEach(s -> mapDataMeta.getContact().add(s));
            } else {
                //noinspection unchecked
                mapDataMeta.setContact(Casts.cast(data.get("telephone"), ArrayList.class));
            }
        }
        //noinspection unchecked
        Map<String, Object> location = Casts.cast(data.get("location"), Map.class);
        mapDataMeta.setPoint(new Point2D.Double(Double.parseDouble(location.get("lat").toString()), Double.parseDouble(location.get("lng").toString())));

        mapDataMeta.setGatherType(GatherTypeEnum.BAIDU_MAP);
        return mapDataMeta;
    }

    private Map<String, Object> exchangeApi(String url, MultiValueMap<String, String> param, String key) {
        String paramString = Casts.castRequestBodyMapToString(param);
        String api = StringUtils.substringAfter(url, API_DOMAIN);
        if (StringUtils.isNotEmpty(paramString)) {
            url = StringUtils.appendIfMissing(url, "?");
        }
        String sn = sign(api, param, key);
        ResponseEntity<String> entity = restTemplate.exchange(url + paramString +"&sn=" + sn + "&timestamp=" + System.currentTimeMillis(), HttpMethod.GET, null, new ParameterizedTypeReference<>(){});

        String text = checkBody(entity);
        //noinspection unchecked
        Map<String, Object> body = Casts.readValue(text, Map.class);

        if (!"0".equals(body.get(SystemConstants.STATUS_TABLE_FLED_NAME).toString())) {
            throw new ServiceException(body.get(RestResult.DEFAULT_MESSAGE_NAME).toString());
        }
        return body;
    }

    private String sign(String api, MultiValueMap<String, String> param, String key) {
        String paramString = Casts.castRequestBodyMapToString(param, (s) -> URLEncoder.encode(s, Charset.defaultCharset()));
        String wholeStr = api + paramString + key;
        String tempStr = URLEncoder.encode(wholeStr, Charset.defaultCharset());
        return DigestUtils.md5DigestAsHex(tempStr.getBytes(Charset.defaultCharset()));
    }

    private String checkBody(ResponseEntity<String> entity) {
        if (HttpStatus.OK.value() != entity.getStatusCode().value()) {
            throw new SystemException("调用百度地图接口出现错误:" + HttpStatus.valueOf(entity.getStatusCode().value()).getReasonPhrase());
        }

        return Objects.requireNonNull(entity.getBody(), "百度地图响应体中找不到数据");
    }

    @Override
    public MapConfigPrepareMeta getDetails() {
        return Casts.of(baiduMapConfig, MapConfigPrepareMeta.class);
    }

    @Override
    public GatherTypeEnum getType() {
        return GatherTypeEnum.BAIDU_MAP;
    }

    @Override
    protected Page<MapDataMeta> doSearch(PageRequest request, String keyword, List<String> city) {
        String key = getKey();

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();

        param.add("ak", key);
        param.add("query", keyword);
        param.add("region", city.get(city.size() - 1));
        param.add("page_size", String.valueOf(request.getSize()));
        param.add("page_num", String.valueOf(request.getNumber() - 1));
        param.add("city_limit", String.valueOf(baiduMapConfig.isCityLimit()));
        param.add("extensions_adcode", String.valueOf(baiduMapConfig.isExtensionsAdCode()));
        param.add("scope", "2");
        param.add("output","json");

        Map<String, Object> body = exchangeApi(QUERY_DATA_URL, param, key);

        Long total = Casts.cast(body.get("total"), Long.class);

        List<Map<String, Object>> data = Casts.cast(body.get("results"));

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
        return baiduMapConfig.getDataCache();
    }

    @Override
    protected CacheProperties getUserSearchCache() {
        return baiduMapConfig.getSearchCache();
    }
}
