package com.github.dactiv.service.dmp.resolver.support;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.service.dmp.domain.meta.MapDataMeta;
import com.github.dactiv.service.dmp.resolver.MapResolver;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.util.DigestUtils;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * redis 存储的地图缓存解析器抽象实现
 *
 * @author maurice.chen
 */
public abstract class RedissonMapResolver implements MapResolver {

    @Setter
    protected RedissonClient redissonClient;

    @Override
    public void createBySearchId(TypeUserDetails<Integer> userDetails, String searchId) {
        CacheProperties cache = getUserSearchCache();
        RList<MapDataMeta> redisList = redissonClient.getList(cache.getName(getUserSearchCacheKey(userDetails, searchId)));
        if (redisList.isExists()) {
            return ;
        }
        redisList.add(null);
        TimeProperties time = cache.getExpiresTime();
        if (Objects.nonNull(time)) {
            redisList.expire(time.toDuration());
        }
    }

    @Override
    public Page<MapDataMeta> search(TypeUserDetails<Integer> userDetails,
                                    String searchId,
                                    PageRequest request,
                                    String keyword,
                                    List<String> city) {
        if (!getDetails().isEnable()) {
            return new Page<>(request, new LinkedList<>());
        }

        CacheProperties userSearchCache = getUserSearchCache();
        RList<MapDataMeta> redisList = redissonClient.getList(userSearchCache.getName(getUserSearchCacheKey(userDetails, searchId)));
        if (!redisList.isExists()) {
            throw new SystemException("找不到 search Id 为 [" + searchId + "] 的缓存数据");
        }

        redisList.remove(null);

        String md5 = DigestUtils.md5DigestAsHex(String.valueOf(Objects.hash(request.getNumber(), keyword, city)).getBytes(Charset.defaultCharset()));
        CacheProperties cache = getMapDataCache();
        String key = cache.getName(md5);

        Page<MapDataMeta> page = null;

        RBucket<Page<MapDataMeta>> dataCacheBucket = redissonClient.getBucket(key);
        if (dataCacheBucket.isExists()) {
            page = dataCacheBucket.get();
        }

        if (Objects.isNull(page)) {
            page = doSearch(request, keyword, city);
            if (CollectionUtils.isNotEmpty(page.getElements())) {
                dataCacheBucket.setAsync(page);
                TimeProperties time = cache.getExpiresTime();
                if (Objects.nonNull(time)) {
                    dataCacheBucket.expire(time.toDuration());
                }
            }
        }

        cacheUserSearchData(userDetails, searchId, page.getElements());

        List<MapDataMeta> dataMetas = convertMapDataMeta(page.getElements(), userDetails);

        page.setElements(dataMetas);
        return page;
    }

    @Override
    public List<MapDataMeta> convertMapDataMeta(List<MapDataMeta> data, TypeUserDetails<Integer> userDetails) {
        return data;
    }

    protected abstract Page<MapDataMeta> doSearch(PageRequest request, String keyword, List<String> city);

    protected abstract CacheProperties getMapDataCache();

    public void cacheUserSearchData(TypeUserDetails<Integer> userDetails, String searchId, List<MapDataMeta> data) {

        if (StringUtils.isEmpty(searchId) || CollectionUtils.isEmpty(data)) {
            return ;
        }

        CacheProperties cache = getUserSearchCache();
        RList<MapDataMeta> redisList = redissonClient.getList(cache.getName(getUserSearchCacheKey(userDetails, searchId)));
        redisList.addAllAsync(data);
        TimeProperties time = cache.getExpiresTime();
        if (Objects.nonNull(time)) {
            redisList.expire(time.toDuration());
        }
    }

    @Override
    public List<MapDataMeta> getUserMapDataMetas(TypeUserDetails<Integer> userDetails, String searchId) {
        CacheProperties cache = getUserSearchCache();
        RList<MapDataMeta> redisList = redissonClient.getList(cache.getName(getUserSearchCacheKey(userDetails, searchId)));
        TimeProperties time = cache.getExpiresTime();
        if (Objects.nonNull(time)) {
            redisList.expire(time.toDuration());
        }
        return convertMapDataMeta(redisList, userDetails);
    }

    protected String getUserSearchCacheKey(TypeUserDetails<Integer> userDetails, String searchId) {
        return userDetails.getUserType() + CacheProperties.DEFAULT_SEPARATOR + userDetails.getUserId() + CacheProperties.DEFAULT_SEPARATOR + searchId;
    }

    @Override
    public void deleteUserSearchCache(TypeUserDetails<Integer> userDetails, String searchId) {
        String key = getUserSearchCacheKey(userDetails, searchId);
        RList<MapDataMeta> redisList = redissonClient.getList(getUserSearchCache().getName(key));
        redisList.deleteAsync();
    }

    protected abstract CacheProperties getUserSearchCache();

}
