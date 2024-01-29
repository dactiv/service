package com.github.dactiv.service.authentication.enumerate;

import com.github.dactiv.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;

import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TokenEndpointAuthenticationSigningAlgorithmTypeEnum implements NameValueEnum<Integer> {

    /**
     * mac 算法
     */
    MAC_ALGORITHM("mac 算法", 10, MacAlgorithm.class),

    /**
     * 签名算法
     */
    SIGNATURE_ALGORITHM("签名算法", 20, SignatureAlgorithm.class),
    ;

    private final String name;

    private final Integer value;

    private final Class<? extends JwsAlgorithm> algorithmClass;

    public static TokenEndpointAuthenticationSigningAlgorithmTypeEnum ofAlgorithmClass(Class<?> algorithmClass) {
        if (Objects.isNull(algorithmClass)) {
            return null;
        }
        for (TokenEndpointAuthenticationSigningAlgorithmTypeEnum type : values()) {
            if (type.getAlgorithmClass().isAssignableFrom(algorithmClass)) {
                return type;
            }
        }
        return null;
    }
}
