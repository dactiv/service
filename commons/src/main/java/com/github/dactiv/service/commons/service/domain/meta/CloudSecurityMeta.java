package com.github.dactiv.service.commons.service.domain.meta;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 云安全元数据信息
 *
 * @author maurice.chen
 */

@Data
public class CloudSecurityMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 932055947756264921L;

    public static final String SECRET_ID_NAME = "secretId";

    public static final String SECURITY_KEY_NAME = "secretKey";

    /**
     * 密钥 id
     */
    private String secretId;

    /**
     * 密钥
     */
    private String secretKey;
}
