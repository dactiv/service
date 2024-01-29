package com.github.dactiv.service.commons.service.config;

import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.AesCipherService;
import com.github.dactiv.framework.crypto.algorithm.cipher.OperationMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * 公共配置信息
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties("dactiv.service.commons")
public class CommonsConfig {

    /**
     * 数据加解密密钥
     */
    private String dataCryptoKey = "SHprakA0MzA5LgAAAAAAAA==";

    /**
     * 重置密码长度
     */
    private int restPasswordLength = 8;

    public String decrypt(String cipherText) {
        ByteSource byteSource = getEcbPkcs5AesCipherService().decrypt(
                Base64.decode(cipherText),
                Base64.decode(getDataCryptoKey())
        );
        return byteSource.obtainString();
    }

    public String encrypt(String plaintext) {
        ByteSource byteSource = getEcbPkcs5AesCipherService().encrypt(
                plaintext.getBytes(),
                Base64.decode(getDataCryptoKey())
        );
        return byteSource.getBase64();
    }

    public String encrypt(String plaintext, String key) {

        byte[] finalKey = new byte[16];
        int i = 0;

        for (byte b : key.getBytes(StandardCharsets.UTF_8)) {
            finalKey[i++ % 16] ^= b;
        }

        SecretKeySpec secretKeySpec = new SecretKeySpec(finalKey, CipherAlgorithmService.AES_ALGORITHM);
        ByteSource byteSource = getEcbPkcs5AesCipherService().encrypt(plaintext.getBytes(StandardCharsets.UTF_8), secretKeySpec.getEncoded());

        return byteSource.getBase64();

    }

    public AesCipherService getEcbPkcs5AesCipherService() {

        AesCipherService aesCipherService = new AesCipherService();
        aesCipherService.setInitializationVectorSize(0);
        aesCipherService.setMode(OperationMode.ECB);
        aesCipherService.setRandomNumberGenerator(null);

        return aesCipherService;
    }
}
