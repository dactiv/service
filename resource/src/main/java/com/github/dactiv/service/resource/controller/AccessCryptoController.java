package com.github.dactiv.service.resource.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessToken;
import com.github.dactiv.framework.crypto.access.token.SignToken;
import com.github.dactiv.framework.crypto.access.token.SimpleExpirationToken;
import com.github.dactiv.framework.crypto.access.token.SimpleToken;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.SimpleByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.RsaCipherService;
import com.github.dactiv.framework.crypto.algorithm.hash.Hash;
import com.github.dactiv.framework.crypto.algorithm.hash.HashAlgorithmMode;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.audit.Auditable;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import com.github.dactiv.service.resource.config.ApplicationConfig;
import com.github.dactiv.service.resource.domain.entity.access.AccessCryptoEntity;
import com.github.dactiv.service.resource.service.access.AccessCryptoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 访问加解密控制器
 *
 * @author maurice.chen
 */
@Slf4j
@RestController
@RequestMapping("access/crypto")
@Plugin(
        name = "访问加解密",
        id = "access_crypto",
        parent = "config",
        icon = "icon-authorization-management",
        authority = "perms[access_crypto:page]",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class AccessCryptoController {

    private final AccessCryptoService accessCryptoService;

    private final MybatisPlusQueryGenerator<AccessCryptoEntity> queryGenerator;

    private final ApplicationConfig applicationConfig;

    private final CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

    /**
     * 获取访问加解密分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[resource_access_crypto:page]')")
    public Page<AccessCryptoEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        return accessCryptoService.findTotalPage(pageRequest, queryGenerator.getQueryWrapperByHttpRequest(request));
    }

    /**
     * 获取访问加解密
     *
     * @param id 访问加解密主键 id
     * @return 访问加解密实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[resource_access_crypto:get]')")
    @Plugin(name = "编辑信息/查看明细", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public AccessCryptoEntity get(@RequestParam Integer id) {
        return accessCryptoService.get(id);
    }

    /**
     * 获取所有通讯加解密
     *
     * @return 通讯加解密 集合
     */
    @GetMapping("getAll")
    @PreAuthorize("hasRole('FEIGN')")
    public List<AccessCryptoEntity> getAll() {
        return accessCryptoService.getAll();
    }

    /**
     * 保存访问加解密
     *
     * @param entity 访问加解密实体
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[resource_access_crypto:save]')")
    @Plugin(name = "添加或保存信息", audit = true, operationDataTrace = true)
    public RestResult<Integer> save(@RequestBody @Valid AccessCryptoEntity entity) {
        accessCryptoService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除访问加解密
     *
     * @param ids 主键值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[resource_access_crypto:delete]')")
    @Plugin(name = "删除信息", audit = true, operationDataTrace = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        accessCryptoService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 获取所有访问加解密
     *
     * @param type 值
     * @return 访问加解密集合
     */
    @GetMapping("findAccessCrypto")
    public Map<String, Object> findAccessCrypto(@RequestParam String type) {

        List<AccessCryptoEntity> accessCryptoList = accessCryptoService.find(
                Wrappers
                        .<AccessCryptoEntity>lambdaQuery()
                        .eq(AccessCrypto::getType, type)
        );

        Map<String, Object> entry = new LinkedHashMap<>();

        accessCryptoList.forEach(a -> {
            Map<String, Object> field = new LinkedHashMap<>();

            field.put(AccessCrypto.DEFAULT_REQUEST_DECRYPT_FIELD_NAME, a.getRequestDecrypt());
            field.put(AccessCrypto.DEFAULT_RESPONSE_ENCRYPT_FIELD_NAME, a.getResponseEncrypt());

            entry.put(StringUtils.removeEnd(a.getValue(), "/**"), field);
        });

        return entry;
    }

    /**
     * 通过 token id 获取访问 token
     *
     * @param id token id
     * @return 访问 token
     */
    @PreAuthorize("hasRole('FEIGN')")
    @GetMapping("obtainAccessToken")
    public AccessToken obtainAccessToken(@RequestParam String id) {
        return accessCryptoService.getAccessTokeBucket(id).get();
    }

    /**
     * 获取公共 token
     *
     * @param securityContext spring 安全上下文
     * @return 访问 token
     */
    @PreAuthorize("isAuthenticated()")
    @RequestMapping("getPublicToken")
    public AccessToken getPublicToken(@CurrentSecurityContext SecurityContext securityContext) {
        Object details = securityContext.getAuthentication().getDetails();
        if (!MobileUserDetails.class.isAssignableFrom(details.getClass())) {
            return createCamouflageToken();
        }
        MobileUserDetails mobileUserDetails = Casts.cast(details);
        return accessCryptoService.getPublicKey(mobileUserDetails.getDeviceIdentified());

    }

    /**
     * 获取访问密钥
     *
     * @param securityContext 安全上下文
     * @param token           token 信息
     * @param key             密钥信息
     * @return 一个或多个的带签名信息的 token
     */
    @PostMapping("getAccessToken")
    @PreAuthorize("isAuthenticated()")
    @Auditable(principal = "token", type = "获取访问密钥")
    public AccessToken getAccessToken(@CurrentSecurityContext SecurityContext securityContext,
                                      @RequestParam String token,
                                      @RequestParam String key) {
        Object details = securityContext.getAuthentication().getDetails();
        if (!MobileUserDetails.class.isAssignableFrom(details.getClass())) {
            return createCamouflageToken();
        }

        MobileUserDetails mobileUserDetails = Casts.cast(details);

        RBucket<SimpleExpirationToken> bucket = accessCryptoService.getPrivateTokenBucket(token);
        // 根据客户端请求过来的 token 获取私有 token， 如果没有。表示存在问题，返回一个伪装成功的 token
        SimpleExpirationToken privateToken = bucket.get();

        String sha1Token = new Hash(HashAlgorithmMode.SHA1.getName(), mobileUserDetails.getDeviceIdentified()).getHex();

        if (privateToken == null || !privateToken.getToken().equals(token) || !sha1Token.equals(token)) {
            return createCamouflageToken();
        }

        if (log.isDebugEnabled()) {
            log.debug("正在生成 access token, 当前 token 为:{}, 客户端密钥为:{}", token, key);
        }

        RsaCipherService rsa = cipherAlgorithmService.getCipherService("RSA");
        // 解析出客户端发给来的密钥
        ByteSource publicKey = rsa.decrypt(Base64.decode(key), privateToken.getKey().obtainBytes());

        // 生成访问加 token
        AccessToken requestToken = accessCryptoService.generateAccessToken(token);

        // 创建带签名校验的请求解密 token，这个是为了让客户端可以通过签名校验是否正确
        SignToken requestSignToken = createSignToken(
                rsa,
                requestToken,
                publicKey,
                privateToken
        );

        bucket.deleteAsync();

        if (log.isDebugEnabled()) {
            log.debug(
                    "生成新的 access token, 给 [{}] 客户端使用, 本次返回未加密信息为: {}, 原文的 AES 密钥为: {}",
                    token,
                    requestSignToken,
                    requestToken.getKey().getBase64());
        }

        return requestSignToken;
    }

    /**
     * 生成访问秘钥
     *
     * @param request http 请求
     * @return 访问秘钥
     */
    @PostMapping("generateAccessToken")
    public AccessToken generateAccessToken(HttpServletRequest request) {
        String deviceId = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);
        return accessCryptoService.generateAccessToken(deviceId);
    }

    /**
     * 创建一个带签名信息的 token
     *
     * @param rsa          rsa 算法服务
     * @param token        访问 token 信息
     * @param publicKey    密钥
     * @param privateToken 私有token
     * @return 带签名信息的 token
     */
    private SignToken createSignToken(RsaCipherService rsa,
                                      AccessToken token,
                                      ByteSource publicKey,
                                      SimpleExpirationToken privateToken) {
        // 将 token 的 key 加密
        ByteSource encryptAccessCryptoKey = rsa.encrypt(token.getKey().obtainBytes(), publicKey.obtainBytes());
        // 创建一个新的临时 token，客户端收到该 token 时候，
        // 对用自身申城的密钥解密出 encryptAccessCryptoKey，得到真正的加密密钥
        SimpleToken temp = SimpleToken.build(token.getType(), token.getName(), encryptAccessCryptoKey);
        temp.setToken(token.getToken());

        // 将加密的对称密钥生成一个签名，让客户端进行验证
        ByteSource byteSourceSign = rsa.sign(encryptAccessCryptoKey.obtainBytes(), privateToken.getKey().obtainBytes());

        return new SignToken(temp, byteSourceSign);
    }

    /**
     * 创建一个伪装成功的访问 token
     *
     * @return 访问 token
     */
    private AccessToken createCamouflageToken() {
        ByteSource byteSource = new SimpleByteSource(String.valueOf(System.currentTimeMillis()));
        return SimpleToken.generate(applicationConfig.getCamouflageAccessCryptoName(), byteSource);
    }
}
