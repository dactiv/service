package com.github.dactiv.service.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import com.github.dactiv.service.message.domain.body.email.EmailMessageBody;
import com.github.dactiv.service.message.domain.entity.EmailMessageEntity;
import com.github.dactiv.service.message.service.support.EmailMessageSender;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * <p>邮件消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("email")
@Plugin(
        name = "邮件消息",
        id = "email",
        parent = "message",
        authority = "perms[message_email:page]",
        icon = "icon-email",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class EmailMessageController {

    private final EmailMessageSender emailMessageSender;

    private final MybatisPlusQueryGenerator<EmailMessageEntity> queryGenerator;

    /**
     * 获取邮件消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[message_email:page]')")
    public Page<EmailMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {

        QueryWrapper<EmailMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return emailMessageSender.getEmailMessageService().findTotalPage(pageRequest, query);
    }

    /**
     * 获取邮件消息
     *
     * @param id 邮件消息主键 ID
     * @return 邮件消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[message_email:get]')")
    @Plugin(name = "编辑信息/查看明细")
    public EmailMessageEntity get(@RequestParam Integer id) {
        return emailMessageSender.getEmailMessageService().get(id);
    }

    /**
     * 删除邮件消息
     *
     * @param ids 邮件消息主键 ID 值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[message_email:delete]')")
    @Idempotent(key = "message:email:delete:[#securityContext.authentication.details.id]")
    @Plugin(name = "删除信息", audit = true, operationDataTrace = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids,
                                @CurrentSecurityContext SecurityContext securityContext) {
        emailMessageSender.getEmailMessageService().deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 发送消息
     *
     * @param body 请求
     * @return rest 结果集
     */
    @PostMapping("send")
    @PreAuthorize("hasAuthority('perms[message_email:send]')")
    @Plugin(name = "发送信息", audit = true, operationDataTrace = true)
    public RestResult<Object> send(@RequestBody EmailMessageBody body,
                                   @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        body.setUserDetails(userDetails.toBasicUserDetails());
        return emailMessageSender.sendMessage(Collections.singletonList(body));
    }

}
