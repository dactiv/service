package com.github.dactiv.service.authentication.domain.meta;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.service.authentication.enumerate.AccessTokenFormatEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 商户 token 设置
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class MerchantTokenSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 5564109001424545218L;

    @NotNull
    private TimeProperties authorizationCodeTimeToLive = TimeProperties.of(5L, TimeUnit.MINUTES);

    @NotNull
    private TimeProperties accessTokenTimeToLive = TimeProperties.of(1L, TimeUnit.DAYS);

    @NotNull
    private AccessTokenFormatEnum accessTokenFormat = AccessTokenFormatEnum.SELF_CONTAINED;

    @NotNull
    private YesOrNo reuseRefreshTokens = YesOrNo.Yes;

    @NotNull
    private TimeProperties refreshTokenTimeToLive = TimeProperties.of(1L, TimeUnit.DAYS);

    @NotNull
    private SignatureAlgorithm idTokenSignatureAlgorithm = SignatureAlgorithm.RS256;

    public static MerchantTokenSettings ofMap(Map<String, Object> settings) {
        MerchantTokenSettings merchantTokenSettings = new MerchantTokenSettings();

        Duration authorizationCodeTimeToLiveDuration = Casts.castIfNotNull(settings.get(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE));
        if (Objects.nonNull(authorizationCodeTimeToLiveDuration)) {
            merchantTokenSettings.setAuthorizationCodeTimeToLive(TimeProperties.of(authorizationCodeTimeToLiveDuration.getSeconds(), TimeUnit.SECONDS));
        }

        Duration accessTokenTimeToLiveDuration = Casts.castIfNotNull(settings.get(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE));
        if (Objects.nonNull(accessTokenTimeToLiveDuration)) {
            merchantTokenSettings.setAccessTokenTimeToLive(TimeProperties.of(accessTokenTimeToLiveDuration.getSeconds(), TimeUnit.SECONDS));
        }

        Duration refreshTokenTimeToLive = Casts.castIfNotNull(settings.get(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE));
        if (Objects.nonNull(refreshTokenTimeToLive)) {
            merchantTokenSettings.setRefreshTokenTimeToLive(TimeProperties.of(refreshTokenTimeToLive.getSeconds(), TimeUnit.SECONDS));
        }

        Object accessTokenFormat = settings.get(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
        if (Objects.nonNull(accessTokenFormat) && OAuth2TokenFormat.class.isAssignableFrom(accessTokenFormat.getClass())) {
            OAuth2TokenFormat tokenFormat = Casts.cast(accessTokenFormat);
            AccessTokenFormatEnum formatEnum = AccessTokenFormatEnum.ofTokenFormat(tokenFormat);
            merchantTokenSettings.setAccessTokenFormat(formatEnum);
        }

        Object idTokenSignatureAlgorithm = settings.get(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM);
        if (Objects.nonNull(idTokenSignatureAlgorithm) && SignatureAlgorithm.class.isAssignableFrom(idTokenSignatureAlgorithm.getClass())) {
            SignatureAlgorithm signatureAlgorithm = Casts.cast(idTokenSignatureAlgorithm);
            merchantTokenSettings.setIdTokenSignatureAlgorithm(signatureAlgorithm);
        }

        return merchantTokenSettings;
    }

    public Map<String, Object> toTokenSettings() {
        Map<String, Object> result = new LinkedHashMap<>();

        Duration authorizationCodeTimeToLiveDuration = authorizationCodeTimeToLive.toDuration();
        result.put(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE, authorizationCodeTimeToLiveDuration);

        Duration accessTokenTimeToLiveDuration = accessTokenTimeToLive.toDuration();
        result.put(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE, accessTokenTimeToLiveDuration);

        Duration refreshTokenTimeToLiveDuration = refreshTokenTimeToLive.toDuration();
        result.put(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE, refreshTokenTimeToLiveDuration);

        result.put(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS, getReuseRefreshTokens().toBoolean());
        result.put(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT, accessTokenFormat.getFormat());
        result.put(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM, idTokenSignatureAlgorithm);

        return result;
    }

}
