package com.github.dactiv.service.authentication.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.alibaba.nacos.common.utils.StringUtils;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.service.authentication.config.AbnormalAreaConfig;
import com.github.dactiv.service.authentication.domain.IpAddressDetails;
import com.github.dactiv.service.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.service.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.domain.meta.LocationIpRegionMeta;
import com.github.dactiv.service.commons.service.domain.meta.TypeIdNameMeta;
import com.github.dactiv.service.commons.service.enumerate.SiteMessageLinkTypeEnum;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfiguration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_authentication_info 的业务逻辑
 *
 * <p>Table: tb_authentication_info - 认证信息表</p>
 *
 * @author maurice.chen
 * @see AuthenticationInfoEntity
 * @since 2021-11-25 02:42:57
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationInfoService {

    private final AbnormalAreaConfig abnormalAreaConfig;

    private final AuthorizationService authorizationService;

    private final MessageServiceFeignClient messageServiceFeignClient;

    private final ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 验证认证信息
     *
     * @param info 认证信息
     */
    public void validAuthenticationInfo(AuthenticationInfoEntity info) {

        SystemUserEntity user = authorizationService.getSystemUser(info);

        if (!IpAddressDetails.class.isAssignableFrom(user.getClass())) {
            return;
        }

        IpAddressDetails ipAddressDetails = Casts.cast(user);
        LocationIpRegionMeta ipRegionMeta = ipAddressDetails.getIpRegionMeta();

        if (Objects.isNull(ipRegionMeta)) {
            ipAddressDetails.setIpRegionMeta(info.getIpRegionMeta());
            authorizationService.updateSystemUser(user, info.getUserType());
            return;
        }

        AuthenticationInfoEntity authenticationInfo = getLastByUserDetails(info);
        if (Objects.isNull(authenticationInfo)) {
            return;
        }

        String userAddress = ipAddressDetails.getIpRegionMeta().getRegionMeta().getShortRegionString();
        String lastAuthenticationAddress = authenticationInfo.getIpRegionMeta().getRegionMeta().getShortRegionString();
        if (StringUtils.equals(userAddress, lastAuthenticationAddress)) {
            return;
        }

        Map<String, Object> link = Map.of(
                TypeIdNameMeta.TYPE_FIELD_NAME, SiteMessageLinkTypeEnum.AUTHENTICATION_INFO.getValue(),
                IdEntity.ID_FIELD_NAME, info.getId()
        );

        Map<String, Object> meta = new LinkedHashMap<>(info.getDevice());
        meta.put(SecurityUserDetailsConstants.IP_REGION_META_KEY, info.getIpRegionMeta().getRegionMeta());
        meta.put(MessageServiceFeignClient.Constants.Site.LINK_META_FIELD, link);

        Map<String, Object> param = MessageServiceFeignClient.createPushableSiteMessage(
                Collections.singletonList(TypeIdNameMeta.ofUserDetails(info)),
                abnormalAreaConfig.getMessageType(),
                abnormalAreaConfig.getTitle(),
                abnormalAreaConfig.getSendContent(),
                meta
        );

        try {

            RestResult<Object> result = messageServiceFeignClient.send(param);

            if (HttpStatus.OK.value() != result.getStatus() && HttpStatus.NOT_FOUND.value() != result.getStatus()) {
                throw new ServiceException(result.getMessage());
            }

        } catch (Exception e) {
            log.error("发送站内信失败", e);
        }

    }

    /**
     * 根据用户明细获取最后一条认证信息
     *
     * @param userDetails 用户明细
     * @return 认证信息表
     */
    public AuthenticationInfoEntity getLastByUserDetails(BasicUserDetails<Integer> userDetails) {

        Query userIdTerm = Query.of(id -> id.term(t -> t.field(BasicUserDetails.USER_ID_FIELD_NAME).value(userDetails.getUserId())));
        Query userTypeTerm = Query.of(id -> id.term(t -> t.field(BasicUserDetails.USER_TYPE_FIELD_NAME).value(userDetails.getUserType())));

        NativeQuery nativeQuery = new NativeQuery(Query.of(q -> q.bool(b -> b.should(userIdTerm, userTypeTerm))))
                .addSort(Sort.by(Sort.Order.desc(NumberIdEntity.CREATION_TIME_FIELD_NAME)))
                .setPageable(Pageable.ofSize(1));

        SearchHits<AuthenticationInfoEntity> searchHits = elasticsearchTemplate.search(
                nativeQuery,
                AuthenticationInfoEntity.class,
                IndexCoordinates.of(AuthenticationInfoEntity.ELASTICSEARCH_INDEX_NAME)
        );

        SearchHit<AuthenticationInfoEntity> searchHit = searchHits.getSearchHits().getFirst();

        return searchHit.getContent();
    }

    public Page<AuthenticationInfoEntity> findPage(PageRequest pageRequest, Map<String, Object> filter) {
        NativeQuery nativeQuery = new NativeQuery(Query.of(q -> q.bool(b -> b.should(this.createBoolQuery(filter)))))
                .addSort(Sort.by(Sort.Order.desc(NumberIdEntity.CREATION_TIME_FIELD_NAME)))
                .setPageable(Pageable.ofSize(pageRequest.getSize()).withPage(pageRequest.getNumber() - 1));

        SearchHits<AuthenticationInfoEntity> searchHits = elasticsearchTemplate.search(
                nativeQuery,
                AuthenticationInfoEntity.class,
                IndexCoordinates.of(AuthenticationInfoEntity.ELASTICSEARCH_INDEX_NAME)
        );
        List<AuthenticationInfoEntity> elements = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new Page<>(pageRequest, elements);
    }

    private List<Query> createBoolQuery(Map<String, Object> filter) {
        List<Query> result = new LinkedList<>();

        if (filter.containsKey("filter_[username_like]")) {
            String username = filter.get("filter_[username_like]").toString();
            result.add(Query.of(q -> q.wildcard(w -> w.field(BasicUserDetails.USERNAME_FIELD_NAME).value(CorsConfiguration.ALL + username + CorsConfiguration.ALL))));
        }

        if (filter.containsKey("filter_[user_type_eq]")) {
            String userType = filter.get("filter_[user_type_eq]").toString();
            result.add(Query.of(q -> q.term(t -> t.field(BasicUserDetails.USERNAME_FIELD_NAME).value(userType))));
        }

        if (filter.containsKey("filter_[creation_time_between]")) {
            List<String> creationTime = Casts.cast(filter.get("filter_[creation_time_between]"));
            result.add(Query.of(q -> q.range(r -> r.gte(JsonData.of(creationTime.getFirst())).lte(JsonData.of(creationTime.get(1))))));
        }

        return result;
    }

    public AuthenticationInfoEntity get(String id) {
        return elasticsearchTemplate.get(id, AuthenticationInfoEntity.class, IndexCoordinates.of(AuthenticationInfoEntity.ELASTICSEARCH_INDEX_NAME));
    }
}
