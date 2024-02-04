package com.github.dactiv.service.authentication.domain.meta;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.service.authentication.enumerate.TokenEndpointAuthenticationSigningAlgorithmTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 商户单点登陆客户端设置
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class MerchantClientSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 5736168817225592493L;

    @NotNull
    private YesOrNo requireProofKey = YesOrNo.No;

    @NotNull
    private YesOrNo requireAuthorizationConsent = YesOrNo.Yes;

    private TimeProperties authorizationConsentExpirationTime = TimeProperties.ofDay(180);

    private String jwkSetUrl;

    private String tokenEndpointAuthenticationSigningAlgorithmValue = MacAlgorithm.HS256.getName();

    private TokenEndpointAuthenticationSigningAlgorithmTypeEnum tokenEndpointAuthenticationSigningAlgorithmType = TokenEndpointAuthenticationSigningAlgorithmTypeEnum.MAC_ALGORITHM;

    public Map<String, Object> toClientSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();

        settings.put(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY, getRequireProofKey().toBoolean());
        settings.put(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT, getRequireAuthorizationConsent().toBoolean());
        settings.put(ConfigurationSettingNames.Client.JWK_SET_URL, getJwkSetUrl());

        if (Objects.nonNull(tokenEndpointAuthenticationSigningAlgorithmType) && StringUtils.isNotEmpty(tokenEndpointAuthenticationSigningAlgorithmValue)) {
            Enum<? extends JwsAlgorithm>[] values = Casts.cast(
                    tokenEndpointAuthenticationSigningAlgorithmType.getAlgorithmClass().getEnumConstants()
            );
            Arrays
                    .stream(values)
                    .map(o -> Casts.cast(o, JwsAlgorithm.class))
                    .filter(a -> a.getName().equals(getTokenEndpointAuthenticationSigningAlgorithmValue()))
                    .findFirst()
                    .ifPresent(ve -> settings.put(ConfigurationSettingNames.Client.TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM, ve));

        }

        return settings;
    }

    public static MerchantClientSettings ofMap(Map<String, Object> settings) {
        MerchantClientSettings merchantClientSettings = new MerchantClientSettings();

        Boolean requireProofKey = Casts.castIfNotNull(settings.get(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY));
        if (Objects.nonNull(requireProofKey)) {
            merchantClientSettings.setRequireProofKey(YesOrNo.ofBoolean(requireProofKey));
        }

        Boolean requireAuthorizationConsent = Casts.castIfNotNull(settings.get(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT));
        if (Objects.nonNull(requireAuthorizationConsent)) {
            merchantClientSettings.setRequireAuthorizationConsent(YesOrNo.ofBoolean(requireAuthorizationConsent));
        }

        Object jwkSetUrl = settings.get(ConfigurationSettingNames.Client.JWK_SET_URL);
        if (Objects.nonNull(jwkSetUrl)) {
            merchantClientSettings.setJwkSetUrl(jwkSetUrl.toString());
        }

        Object tokenEndpointAuthenticationSigningAlgorithm = settings.get(ConfigurationSettingNames.Client.TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM);
        if (Objects.nonNull(tokenEndpointAuthenticationSigningAlgorithm)) {
            TokenEndpointAuthenticationSigningAlgorithmTypeEnum type = TokenEndpointAuthenticationSigningAlgorithmTypeEnum.ofAlgorithmClass(tokenEndpointAuthenticationSigningAlgorithm.getClass());

            merchantClientSettings.setTokenEndpointAuthenticationSigningAlgorithmType(type);
            JwsAlgorithm jwsAlgorithm = Casts.cast(tokenEndpointAuthenticationSigningAlgorithm);
            merchantClientSettings.setTokenEndpointAuthenticationSigningAlgorithmValue(jwsAlgorithm.getName());
        }

        return merchantClientSettings;
    }
}
