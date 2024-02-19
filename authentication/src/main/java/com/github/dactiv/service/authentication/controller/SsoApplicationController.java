package com.github.dactiv.service.authentication.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.service.authentication.domain.entity.MerchantClientEntity;
import com.github.dactiv.service.authentication.domain.entity.SsoApplicationEntity;
import com.github.dactiv.service.authentication.service.MerchantClientService;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * tb_sso_application 的控制器
 *
 * <p>Table: tb_sso_application - 单点登陆应用</p>
 *
 * @author maurice.chen
 * @see SsoApplicationEntity
 * @since 2023-11-29 10:22:33
 */
@RestController
@RequestMapping("sso/app")
@Plugin(
        name = "单点登陆应用管理",
        id = "sso_application",
        parent = "merchant_client",
        icon = "icon-application",
        authority = "perms[sso_application:page]",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class SsoApplicationController {

    private final MerchantClientService merchantClientService;

    private final MybatisPlusQueryGenerator<SsoApplicationEntity> queryGenerator;

    @PostMapping("find")
    @Plugin(name = "查找单点登陆应用")
    @PreAuthorize("hasAuthority('perms[authentication_sso_application:find]')")
    public List<SsoApplicationEntity> find(HttpServletRequest request) {
        QueryWrapper<SsoApplicationEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return merchantClientService.findSsoApplication(query);
    }

    /**
     * 获取 table: tb_sso_application 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see SsoApplicationEntity
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[authentication_sso_application:page]')")
    public Page<SsoApplicationEntity> page(PageRequest pageRequest,
                                           @RequestParam String merchantClientId,
                                           HttpServletRequest request) {

        QueryWrapper<SsoApplicationEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.eq(MerchantClientEntity.MERCHANT_CLIENT_ID_TABLE_FIELD_NAME, merchantClientId);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return merchantClientService.findSsoApplicationPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_sso_application 实体
     *
     * @param id 主键 ID
     * @return tb_sso_application 实体
     * @see SsoApplicationEntity
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[authentication_sso_application:get]')")
    @Plugin(name = "编辑信息")
    public SsoApplicationEntity get(@RequestParam Integer id) {
        return merchantClientService.getSsoApplication(id);
    }

    /**
     * 保存 table: tb_sso_application 实体
     *
     * @param entity tb_sso_application 实体
     * @see SsoApplicationEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[authentication_sso_application:save]')")
    @Plugin(name = "保存或添加信息", audit = true, operationDataTrace = true)
    public RestResult<Integer> save(@Valid @RequestBody SsoApplicationEntity entity) {
        merchantClientService.saveSsoApplication(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_sso_application 实体
     *
     * @param ids 主键 ID 值集合
     * @see SsoApplicationEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[authentication_sso_application:delete]')")
    @Plugin(name = "删除信息", audit = true, operationDataTrace = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        merchantClientService.deleteSsoApplication(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
