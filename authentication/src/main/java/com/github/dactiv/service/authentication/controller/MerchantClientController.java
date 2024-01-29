package com.github.dactiv.service.authentication.controller;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.service.authentication.domain.entity.MerchantClientEntity;
import com.github.dactiv.service.authentication.service.MerchantClientService;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


/**
 * tb_merchant_client 的控制器
 *
 * <p>Table: tb_merchant_client - 商户 OAuth 2 客户端注册信息</p>
 *
 * @author maurice.chen
 * @see MerchantClientEntity
 * @since 2023-11-23 08:46:39
 */
@RestController
@RequestMapping("merchant/client")
@Plugin(
        name = "商户 OAuth 2 客户端管理",
        id = "merchant_client",
        authority = "perms[merchant_client:get_by_merchant_id]",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class MerchantClientController {

    private final MerchantClientService merchantClientService;

    /**
     * 获取 table: tb_merchant_client 实体
     *
     * @param id 主键 ID
     * @return tb_merchant_client 实体
     * @see MerchantClientEntity
     */
    @GetMapping("getByMerchantId")
    @PreAuthorize("hasAuthority('perms[merchant_client:get_by_merchant_id]')")
    public MerchantClientEntity getByMerchantId(@RequestParam Integer id) {
        return merchantClientService.getByMerchantId(id);
    }

    /**
     * 保存 table: tb_merchant_client 实体
     *
     * @param entity tb_merchant_client 实体
     * @see MerchantClientEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[merchant_client:save]')")
    @Plugin(name = "保存或添加信息", audit = true, operationDataTrace = true)
    public RestResult<Integer> save(@Valid @RequestBody MerchantClientEntity entity) {
        merchantClientService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

}
