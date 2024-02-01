package com.github.dactiv.service.authentication.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.service.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.service.authentication.service.ConsoleUserService;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户控制器
 *
 * @author maurice.chen
 **/
@RestController
@RequestMapping("console/user")
@Plugin(
        name = "用户管理",
        id = "console_user",
        parent = "authority",
        icon = "icon-administrator",
        authority = "perms[console_user:page]",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class ConsoleUserController {

    private final ConsoleUserService consoleUserService;

    private final MybatisPlusQueryGenerator<ConsoleUserEntity> queryGenerator;

    /**
     * 查找系统用户分页信息
     *
     * @param pageRequest 分页请求
     * @param request     http 请求
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[console_user:page]')")
    public Page<ConsoleUserEntity> page(PageRequest pageRequest, HttpServletRequest request) {

        QueryWrapper<ConsoleUserEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return consoleUserService.findTotalPage(pageRequest, query);
    }

    /**
     * 查找系统用户信息
     *
     * @param request http 请求
     * @return 系统用户信息
     */
    @PostMapping("find")
    @PreAuthorize("isAuthenticated()")
    public List<ConsoleUserEntity> find(HttpServletRequest request) {

        QueryWrapper<ConsoleUserEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return consoleUserService.find(query);
    }

    /**
     * 获取系统用户实体
     *
     * @param id 主键值
     * @return 用户实体
     */
    @GetMapping("get")
    @PreAuthorize("hasRole('FEIGN') or hasAuthority('perms[console_user:get]')")
    @Plugin(name = "编辑信息/查看明细")
    public ConsoleUserEntity get(@RequestParam Integer id) {

        return consoleUserService.get(id);
    }

    /**
     * 保存系统用户实体
     *
     * @param entity 系统用户实体
     * @return 消息结果集
     */
    @PostMapping("save")
    @Plugin(name = "添加或保存信息", audit = true, operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[console_user:save]')")
    public RestResult<Integer> save(@Valid @RequestBody ConsoleUserEntity entity) {
        consoleUserService.save(entity);

        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除系统用户
     *
     * @param ids 系统用户主键 ID 集合
     * @return 消息结果集
     */
    @PostMapping("delete")
    @Plugin(name = "删除信息", audit = true, operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[console_user:delete]')")
    public RestResult<?> delete(@RequestParam List<Integer> ids) {

        consoleUserService.deleteById(ids, false);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 判断登录账户是否唯一
     *
     * @param username 登录账户
     * @return true 是，否则 false
     */
    @GetMapping("isUsernameUnique")
    @PreAuthorize("isAuthenticated()")
    public Boolean isUsernameUnique(@RequestParam String username) {
        return !consoleUserService
                .lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getUsername, username)
                .exists();
    }

    /**
     * 判断邮件手机号码
     *
     * @param phoneNumber 电子邮件
     * @return true 是，否则 false
     */
    @GetMapping("isPhoneNumberUnique")
    @PreAuthorize("isAuthenticated()")
    public Boolean isPhoneNumberUnique(@RequestParam String phoneNumber) {
        return !consoleUserService
                .lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getPhoneNumber, phoneNumber)
                .exists();
    }

    /**
     * 判断邮件是否唯一
     *
     * @param email 电子邮件
     * @return true 是，否则 false
     */
    @GetMapping("isEmailUnique")
    @PreAuthorize("isAuthenticated()")
    public Boolean isEmailUnique(@RequestParam String email) {
        return !consoleUserService
                .lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getEmail, email)
                .exists();
    }
}
