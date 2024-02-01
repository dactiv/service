package com.github.dactiv.service.authentication.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.service.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.service.authentication.service.AuthenticationInfoService;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 认证信息控制器
 *
 * @author maurice
 */
@RestController
@RequestMapping("authentication/info")
@Plugin(
        name = "登录日志查询",
        parent = "log",
        authority = "perms[authentication_info:page]",
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE,
        type = ResourceType.Menu,
        icon = "icon-user-export"
)
@RequiredArgsConstructor
public class AuthenticationInfoController {

    private final AuthenticationInfoService authenticationInfoService;

    /**
     * 获取认证信息表分页信息
     *
     * @param pageRequest 分页请求
     * @param request     http 请求
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[authentication_info:page]')")
    public Page<AuthenticationInfoEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        return authenticationInfoService.findPage(
                pageRequest,
                Casts.castArrayValueMapToObjectValueMap(request.getParameterMap())
        );
    }

    /**
     * 获取认证信息实体
     *
     * @param id 主键值
     * @return 认证信息实体
     */
    @GetMapping("get")
    @Plugin(name = "查看详情")
    @PreAuthorize("hasAuthority('perms[authentication_info:get]')")
    public AuthenticationInfoEntity get(@RequestParam String id) {
        return authenticationInfoService.get(id);
    }

    /**
     * 获取认证信息实体
     *
     * @param userDetails 用户明细
     * @return 认证信息实体
     */
    @PostMapping("getLastByUserDetails")
    @PreAuthorize("hasRole('FEIGN')")
    public AuthenticationInfoEntity getLastByUserDetails(@RequestBody BasicUserDetails<Integer> userDetails) {
        return authenticationInfoService.getLastByUserDetails(userDetails);
    }

}
