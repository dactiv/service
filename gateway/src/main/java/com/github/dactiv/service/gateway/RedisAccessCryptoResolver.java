package com.github.dactiv.service.gateway;

import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessToken;
import com.github.dactiv.framework.nacos.task.annotation.NacosCronScheduled;
import com.github.dactiv.service.gateway.config.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RListReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * redis 访问加解密解析器实现
 *
 * @author maurice
 */
@Slf4j
@Component
public class RedisAccessCryptoResolver extends AbstractAccessCryptoResolver implements InitializingBean {

    private final RedissonReactiveClient redissonClient;

    private final ApplicationConfig applicationConfig;

    private final List<AccessCrypto> cache = new ArrayList<>();

    public RedisAccessCryptoResolver(ApplicationConfig config,
                                     ObjectProvider<RoutePredicateFactory<Object>> predicates,
                                     ObjectProvider<CipherAlgorithmService> cipherAlgorithmService,
                                     RedissonReactiveClient redissonClient,
                                     ApplicationConfig applicationConfig) {
        super(config, predicates.stream().collect(Collectors.toList()), cipherAlgorithmService);
        this.redissonClient = redissonClient;
        this.applicationConfig = applicationConfig;
    }

    @Override
    protected Mono<AccessToken> getAccessToken(String accessToken) {
        RBucketReactive<AccessToken> bucket = redissonClient.getBucket(applicationConfig.getAccessTokenKeyCache().getName(accessToken));
        return bucket.get();
    }

    @Override
    public List<AccessCrypto> getAccessCryptoList() {
        return cache;
    }

    @Override
    @NacosCronScheduled(cron = "${dactiv.service.gateway.crypto.access.sync-cron:0 0/3 * * * ?}")
    public void afterPropertiesSet() {
        RListReactive<AccessCrypto> accessTokens = redissonClient
                .getList(applicationConfig.getAccessCryptoCache().getName());
        accessTokens.iterator().next().subscribe(this::addAccessCryptoCache);
    }

    private void addAccessCryptoCache(AccessCrypto accessCrypto) {
        List<String> predicateString = accessCrypto
                .getPredicates()
                .stream()
                .map(p -> MessageFormat.format("name = {0}, value={1} ", p.getName(), p.getValue()))
                .collect(Collectors.toList());

        log.info(
                "[name={},type={}}]:{} = [{}]",
                accessCrypto.getName(),
                accessCrypto.getType(),
                accessCrypto.getValue(),
                StringUtils.join(predicateString, StringArrayPropertyEditor.DEFAULT_SEPARATOR)
        );

        cache.add(accessCrypto);
    }
}
