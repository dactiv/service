package com.github.dactiv.service.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import com.github.dactiv.service.message.domain.body.site.SiteMessageBody;
import com.github.dactiv.service.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.service.message.service.support.SiteMessageSender;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>站内信消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("site")
@Plugin(
        name = "站内信消息",
        id = "site",
        parent = "message",
        authority = "perms[site:page]",
        icon = "icon-bell",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class SiteMessageController {

    private final SiteMessageSender siteMessageSender;

    private final MybatisPlusQueryGenerator<SiteMessageEntity> queryGenerator;

    /**
     * 获取站内信消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     过滤条件
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[message_site:page]')")
    public Page<SiteMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<SiteMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return siteMessageSender.getSiteMessageService().findTotalPage(pageRequest, query);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("pageForFrontEnd")
    public Page<SiteMessageEntity> pageForFrontEnd(PageRequest pageRequest,
                                                   @RequestParam Integer type,
                                                   @CurrentSecurityContext SecurityContext securityContext) {

        MessageTypeEnum typeEnum = ValueEnumUtils.parse(type, MessageTypeEnum.class);
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        return siteMessageSender.getSiteMessageService().pageForFrontEnd(pageRequest, typeEnum, userDetails.toBasicUserDetails());
    }

    @PostMapping("pageByPrincipal")
    @PreAuthorize("isAuthenticated()")
    public Page<SiteMessageEntity> pageByPrincipal(@CurrentSecurityContext SecurityContext securityContext,
                                                   PageRequest pageRequest,
                                                   HttpServletRequest request) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        QueryWrapper<SiteMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);

        query.eq(SecurityUserDetailsConstants.USER_ID_TABLE_FIELD, userDetails.getId());
        query.eq(SecurityUserDetailsConstants.USER_TYPE_TABLE_FIELD, userDetails.getType());

        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return siteMessageSender.getSiteMessageService().findTotalPage(pageRequest, query);
    }

    /**
     * 按类型分组获取站内信未读数量
     *
     * @param securityContext 安全上下文
     * @return 按类型分组的未读数量
     */
    @GetMapping("unreadQuantity")
    @PreAuthorize("isAuthenticated()")
    public Map<Integer, Long> unreadQuantity(@CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        return siteMessageSender.getSiteMessageService().countUnreadQuantity(userDetails.toBasicUserDetails());
    }

    /**
     * 阅读站内信
     *
     * @param types           站内信类型
     * @param securityContext 安全上下文
     * @return 消息结果集
     */
    @PostMapping("readAll")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> readAll(@RequestParam(required = false) List<Integer> types,
                                 @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        siteMessageSender.getSiteMessageService().read(types, userDetails);
        return RestResult.of("标记所有为已读成功");
    }

    @PostMapping("deleteRead")
    @PreAuthorize("isAuthenticated()")
    public RestResult<?> deleteRead(@RequestParam(required = false) List<Integer> types,
                                    @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        siteMessageSender.getSiteMessageService().deleteRead(types, userDetails.toBasicUserDetails());
        return RestResult.of("删除所有已读信息成功");

    }

    @PostMapping("read")
    @PreAuthorize("isAuthenticated()")
    public SiteMessageEntity read(@RequestParam Integer id, @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        return siteMessageSender.getSiteMessageService().read(id, userDetails);
    }

    /**
     * 获取站内信消息
     *
     * @param id 站内信消息主键 ID
     * @return 站内信消息实体
     */
    @GetMapping("get")
    @Plugin(name = "编辑信息/查看明细")
    @PreAuthorize("hasAuthority('perms[message_site:get]')")
    public SiteMessageEntity get(@RequestParam Integer id) {
        return siteMessageSender.getSiteMessageService().get(id);
    }

    @PostMapping("getForFrontEnd")
    @PreAuthorize("isAuthenticated()")
    public SiteMessageEntity getForFrontEnd(@RequestParam Integer id,
                                            @RequestParam Integer read,
                                            @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        YesOrNo readValue = ValueEnumUtils.parse(read, YesOrNo.class);
        return siteMessageSender.getSiteMessageService().getForFrontEnd(id, readValue, userDetails.toBasicUserDetails());
    }

    /**
     * 删除站内信消息
     *
     * @param ids 站内信消息主键 ID 值集合
     * @return 消息结果集
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[message_site:delete]')")
    @Plugin(name = "删除信息", audit = true, operationDataTrace = true)
    @Idempotent(key = "message:site:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        siteMessageSender.getSiteMessageService().deleteById(ids, userDetails);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    @PostMapping("send")
    @PreAuthorize("hasAuthority('perms[site:send]')")
    @Plugin(name = "发送站内信", audit = true, operationDataTrace = true)
    public RestResult<Object> send(@RequestBody SiteMessageBody siteMessageBody) {
        return siteMessageSender.sendMessage(Collections.singletonList(siteMessageBody));
    }
}
