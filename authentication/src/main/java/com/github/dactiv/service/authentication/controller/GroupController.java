package com.github.dactiv.service.authentication.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.service.authentication.domain.entity.GroupEntity;
import com.github.dactiv.service.authentication.service.GroupService;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户用户组控制器
 *
 * @author maurice.chen
 **/
@RestController
@RequestMapping("group")
@Plugin(
        name = "角色管理",
        id = "group",
        parent = "authority",
        authority = "perms[group:find]",
        icon = "icon-role-management",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    private final MybatisPlusQueryGenerator<GroupEntity> queryGenerator;

    /**
     * 获取所有用户组
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[authentication_group:find]')")
    public List<GroupEntity> find(HttpServletRequest request, @RequestParam(required = false) boolean mergeTree) {
        QueryWrapper<GroupEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        List<GroupEntity> groupList = groupService.find(query);

        if (mergeTree) {
            return TreeUtils.buildGenericTree(groupList);
        } else {
            return groupList;
        }
    }

    /**
     * 获取用户组
     *
     * @param id 主键值
     * @return 用户组实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[authentication_group:get]')")
    @Plugin(name = "编辑信息/查看明细")
    public GroupEntity get(@RequestParam Integer id) {
        return groupService.get(id);
    }

    /**
     * 保存用户组
     *
     * @param entity 用户组实体
     * @return 消息结果集
     */
    @PostMapping("save")
    @PreAuthorize("hasAuthority('perms[authentication_group:save]')")
    @Plugin(name = "添加或保存信息", audit = true, operationDataTrace = true)
    public RestResult<Integer> save(@Valid @RequestBody GroupEntity entity) {

        groupService.save(entity);

        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除用户组
     *
     * @param ids 主键值集合
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[authentication_group:delete]')")
    @Plugin(name = "删除信息", audit = true, operationDataTrace = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {

        groupService.deleteById(ids);

        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 判断 spring security role 的 authority 值是否唯一
     *
     * @param authority spring security role 的 authority 值
     * @return true 是，否则 false
     */
    @GetMapping("isAuthorityUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isAuthorityUnique(@RequestParam String authority) {
        return !groupService.lambdaQuery().eq(GroupEntity::getAuthority, authority).exists();
    }

    /**
     * 判断组名称是否唯一
     *
     * @param name 组名称
     * @return true 唯一，否则 false
     */
    @GetMapping("isNameUnique")
    @PreAuthorize("isAuthenticated()")
    public boolean isNameUnique(@RequestParam String name) {
        return !groupService.lambdaQuery().eq(GroupEntity::getName, name).exists();
    }
}
