package com.github.dactiv.service.resource.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import com.github.dactiv.service.resource.domain.entity.MerchantEntity;
import com.github.dactiv.service.resource.service.MerchantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * tb_merchant 的控制器
 *
 * <p>Table: tb_merchant - 商户表</p>
 *
 * @author maurice.chen
 * @see MerchantEntity
 * @since 2023-09-11 08:57:11
 */
@RestController
@RequestMapping("merchant")
@Plugin(
        name = "商户管理",
        id = "merchant",
        authority = "perms[merchant:page]",
        icon = "icon-merchant-list",
        parent = "config",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    private final MybatisPlusQueryGenerator<MerchantEntity> queryGenerator;

    /**
     * 获取 table: tb_merchant 实体集合
     *
     * @param request http servlet request
     * @return tb_merchant 实体集合
     * @see MerchantEntity
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<MerchantEntity> find(HttpServletRequest request) {
        QueryWrapper<MerchantEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return merchantService.find(query);
    }

    /**
     * 获取 table: tb_merchant 分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see MerchantEntity
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[resource_merchant:page]')")
    public Page<MerchantEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<MerchantEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return merchantService.findTotalPage(pageRequest, query);
    }

    /**
     * 获取 table: tb_merchant 实体
     *
     * @param id 主键 ID
     * @return tb_merchant 实体
     * @see MerchantEntity
     */
    @GetMapping("get")
    @Plugin(name = "编辑信息")
    @PreAuthorize("hasAuthority('perms[resource_merchant:get]') or hasRole('FEIGN')")
    public MerchantEntity get(@RequestParam Integer id) {
        return merchantService.get(id);
    }

    /**
     * 保存 table: tb_merchant 实体
     *
     * @param entity tb_merchant 实体
     * @see MerchantEntity
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[resource_merchant:save]')")
    @Plugin(name = "保存或添加信息", audit = true, operationDataTrace = true)
    public RestResult<Integer> save(@Valid @RequestBody MerchantEntity entity) {
        merchantService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除 table: tb_merchant 实体
     *
     * @param ids 主键 ID 值集合
     * @see MerchantEntity
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[resource_merchant:delete]')")
    @Plugin(name = "删除信息", audit = true, operationDataTrace = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        merchantService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
