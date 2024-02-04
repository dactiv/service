package com.github.dactiv.service.authentication.domain.entity;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.id.StringIdEntity;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.interceptor.audit.support.InMemoryOperationDataTraceRepository;
import com.github.dactiv.service.authentication.domain.meta.MerchantClientSettings;
import com.github.dactiv.service.authentication.domain.meta.MerchantTokenSettings;
import com.github.dactiv.service.authentication.enumerate.AuthorizationGrantTypeEnum;
import com.github.dactiv.service.authentication.enumerate.ClientAuthenticationMethodEnum;
import com.github.dactiv.service.authentication.enumerate.RegisteredClientScopeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.type.Alias;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.io.Serial;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>Table: tb_merchant_client 商家 OAuth 2 客户端注册信息- </p>
 *
 * @author maurice.chen
 * @since 2023-11-22 03:21:13
 */
@Data
@NoArgsConstructor
@Alias("merchantRegisteredClient")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_merchant_client", autoResultMap = true)
public class MerchantClientEntity extends StringIdEntity {

    @Serial
    private static final long serialVersionUID = 9094793840568094862L;

    public static final String DOMAIN_REGX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    public static final String MERCHANT_APP_ID_NAME = "appId";

    public static final String MERCHANT_APP_KEY_NAME = "appKey";

    public static final String MERCHANT_CLIENT_ID_TABLE_FIELD_NAME = "merchant_client_id";

    public static final String MERCHANT_CREATION_TIME_NAME = "creation_time";

    public static final String[] IGNORE_PROPERTIES = {"id", "creation_time", "version", "enable"};

    public static final String BUILDER_VALIDATE_SCOPES_METHOD_NAME = "validateScopes";

    public static final String BUILDER_CREATE_METHOD_NAME = "create";

    /**
     * 版本号
     */
    @Version
    private Integer version;

    /**
     * 商户 id
     */
    @NotNull
    private Integer merchantId;

    /**
     * 客户端 id
     */
    @NotNull
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String clientId;

    /**
     * 客户端 id发放时间
     */
    @NotNull
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Date clientIdIssuedAt;

    /**
     * 客户端密钥
     */
    @NotNull
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String clientSecret;

    /**
     * 客户端密钥过期时间
     */
    @NotNull
    private Date clientSecretExpiresAt;

    /**
     * 客户端名称
     */
    @NotNull
    private String clientName;

    /**
     * 是否启用
     */
    @NotNull
    private YesOrNo enable;

    /**
     * 授权方法
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @JsonCollectionGenericType(ClientAuthenticationMethodEnum.class)
    private List<ClientAuthenticationMethodEnum> clientAuthenticationMethods = new LinkedList<>();

    /**
     * 认证类型
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @JsonCollectionGenericType(AuthorizationGrantTypeEnum.class)
    private List<AuthorizationGrantTypeEnum> authorizationGrantTypes = new LinkedList<>();

    /**
     * 重定向 url
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Set<String> redirectUris = new HashSet<>();

    /**
     * 授权作用域
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @JsonCollectionGenericType(RegisteredClientScopeEnum.class)
    private Set<RegisteredClientScopeEnum> scopes = new HashSet<>();

    /**
     * 客户端设置
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private MerchantClientSettings clientSettings = new MerchantClientSettings();

    /**
     * token 设置
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private MerchantTokenSettings tokenSettings = new MerchantTokenSettings();

    public RegisteredClient toRegisteredClient() {
        RegisteredClient.Builder builder = RegisteredClient
                .withId(getId())
                .clientName(getClientName())
                .clientId(getClientId())
                .clientIdIssuedAt(getClientIdIssuedAt().toInstant())
                .clientSecret(getClientSecret())
                .clientSecretExpiresAt(getClientSecretExpiresAt().toInstant());

        if (CollectionUtils.isNotEmpty(getScopes())) {
            builder.scopes(s -> s.addAll(getScopes().stream().map(RegisteredClientScopeEnum::getValue).collect(Collectors.toSet())));
        }

        if (CollectionUtils.isNotEmpty(getRedirectUris())) {
            builder.redirectUris(s -> s.addAll(getRedirectUris()));
        }

        if (CollectionUtils.isNotEmpty(getClientAuthenticationMethods())) {
            Set<ClientAuthenticationMethod> methods = getClientAuthenticationMethods()
                    .stream()
                    .map(ClientAuthenticationMethodEnum::getValue)
                    .collect(Collectors.toSet());
            builder.clientAuthenticationMethods(m -> m.addAll(methods));
        }

        if (CollectionUtils.isNotEmpty(getAuthorizationGrantTypes())) {
            Set<AuthorizationGrantType> types = getAuthorizationGrantTypes()
                    .stream()
                    .map(AuthorizationGrantTypeEnum::getValue)
                    .collect(Collectors.toSet());
            builder.authorizationGrantTypes(t -> t.addAll(types));
        }

        builder.clientSettings(ClientSettings.withSettings(getClientSettings().toClientSettings()).build());

        builder.tokenSettings(TokenSettings.withSettings(getTokenSettings().toTokenSettings()).build());
        ReflectionUtils.invokeMethod(builder, BUILDER_VALIDATE_SCOPES_METHOD_NAME, new LinkedList<>());

        RegisteredClient registeredClient = Casts.cast(ReflectionUtils.invokeMethod(builder, BUILDER_CREATE_METHOD_NAME, new LinkedList<>()));
        if (CollectionUtils.isNotEmpty(registeredClient.getRedirectUris())) {
            validRedirectUris(getRedirectUris());
        }
        return registeredClient;
    }

    private static void validRedirectUris(Set<String> redirectUris) {
        List<String> urls = redirectUris.stream().filter(s -> !s.matches(DOMAIN_REGX)).collect(Collectors.toList());
        Assert.isTrue(CollectionUtils.isEmpty(urls), "url: " + urls + " 非域名地址");
    }

    public static MerchantClientEntity ofRegisteredClient(RegisteredClient client) {
        MerchantClientEntity entity = new MerchantClientEntity();

        entity.setId(client.getId());
        entity.setClientId(client.getClientId());
        entity.setClientSecret(client.getClientSecret());

        if (Objects.nonNull(client.getClientSecretExpiresAt())) {
            entity.setClientSecretExpiresAt(Date.from(client.getClientSecretExpiresAt()));
        }

        if (Objects.nonNull(client.getClientIdIssuedAt())) {
            entity.setClientIdIssuedAt(Date.from(client.getClientIdIssuedAt()));
        }

        if (CollectionUtils.isNotEmpty(client.getScopes())) {
            entity.setScopes(client.getScopes().stream().map(s -> ValueEnumUtils.parse(s, RegisteredClientScopeEnum.class)).collect(Collectors.toSet()));
        }

        if (CollectionUtils.isNotEmpty(client.getRedirectUris())) {
            entity.setRedirectUris(client.getRedirectUris());
        }

        if (CollectionUtils.isNotEmpty(client.getClientAuthenticationMethods())) {
            List<ClientAuthenticationMethodEnum> methods = client
                    .getClientAuthenticationMethods()
                    .stream()
                    .map(ClientAuthenticationMethodEnum::ofValue)
                    .collect(Collectors.toList());
            entity.setClientAuthenticationMethods(methods);
        }

        if (CollectionUtils.isNotEmpty(client.getAuthorizationGrantTypes())) {
            List<AuthorizationGrantTypeEnum> types = client
                    .getAuthorizationGrantTypes()
                    .stream()
                    .map(AuthorizationGrantTypeEnum::ofValue)
                    .collect(Collectors.toList());
            entity.setAuthorizationGrantTypes(types);
        }

        if (Objects.nonNull(client.getClientSettings()) && MapUtils.isNotEmpty(client.getClientSettings().getSettings())) {
            entity.setClientSettings(MerchantClientSettings.ofMap(client.getClientSettings().getSettings()));
        }

        if (Objects.nonNull(client.getTokenSettings()) && MapUtils.isNotEmpty(client.getTokenSettings().getSettings())) {
            entity.setTokenSettings(MerchantTokenSettings.ofMap(client.getTokenSettings().getSettings()));
        }

        if (StringUtils.isEmpty(entity.getId())) {
            entity.setId(DigestUtils.md5DigestAsHex(client.getClientId().getBytes()));
            entity.setCreationTime(new Date());
        }

        return entity;

    }

    public static MerchantClientEntity ofFlatMessageData(Map<String, Object> data) {
        MerchantClientEntity entity = new MerchantClientEntity();

        entity.setMerchantId(Casts.castIfNotNull(data.get(IdEntity.ID_FIELD_NAME), Integer.class));
        entity.setClientName(data.getOrDefault(FileObject.MINIO_FILE_NAME, StringUtils.EMPTY).toString());
        entity.setClientId(data.getOrDefault(MERCHANT_APP_ID_NAME, StringUtils.EMPTY).toString());

        String creationTimeString = data.getOrDefault(MERCHANT_CREATION_TIME_NAME, StringUtils.EMPTY).toString();
        if (StringUtils.isNotEmpty(creationTimeString)) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                    .ofPattern(InMemoryOperationDataTraceRepository.DEFAULT_DATE_FORMATTER_PATTERN);
            Instant instant = LocalDateTime
                    .parse(creationTimeString, dateTimeFormatter)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            entity.setClientIdIssuedAt(Date.from(instant));
        }

        entity.setClientSecret(data.getOrDefault(MERCHANT_APP_KEY_NAME, StringUtils.EMPTY).toString());
        entity.setEnable(YesOrNo.No);

        if (StringUtils.isEmpty(entity.getId())) {
            entity.setId(DigestUtils.md5DigestAsHex(entity.getClientId().getBytes()));
            entity.setCreationTime(new Date());
        }

        Set<RegisteredClientScopeEnum> registeredClientScopes = Arrays
                .stream(RegisteredClientScopeEnum.values())
                .filter(RegisteredClientScopeEnum::isDefault)
                .collect(Collectors.toSet());
        entity.setScopes(registeredClientScopes);

        List<AuthorizationGrantTypeEnum> authorizationGrantTypes = Arrays
                .stream(AuthorizationGrantTypeEnum.values())
                .filter(AuthorizationGrantTypeEnum::isDefault)
                .collect(Collectors.toList());
        entity.setAuthorizationGrantTypes(authorizationGrantTypes);

        List<ClientAuthenticationMethodEnum> clientAuthenticationMethods = Arrays
                .stream(ClientAuthenticationMethodEnum.values())
                .filter(ClientAuthenticationMethodEnum::isDefault)
                .collect(Collectors.toList());
        entity.setClientAuthenticationMethods(clientAuthenticationMethods);

        return entity;
    }
}