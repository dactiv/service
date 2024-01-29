package com.github.dactiv.service.message.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import com.github.dactiv.service.message.domain.body.sms.SmsMessageBody;
import com.github.dactiv.service.message.domain.entity.SmsMessageEntity;
import com.github.dactiv.service.message.domain.meta.sms.SmsBalanceMeta;
import com.github.dactiv.service.message.service.support.SmsMessageSender;
import com.github.dactiv.service.message.service.support.sms.SmsChannelSender;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>短信消息控制器</p>
 *
 * @author maurice
 * @since 2020-04-06 10:16:10
 */
@Slf4j
@RestController
@RequestMapping("sms")
@Plugin(
        name = "短信消息",
        id = "sms",
        parent = "message",
        authority = "perms[sms:page]",
        icon = "icon-sms",
        type = ResourceType.Security,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class SmsMessageController {

    private final SmsMessageSender smsMessageSender;

    private final MybatisPlusQueryGenerator<SmsMessageEntity> queryGenerator;

    /**
     * 获取短信消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     过滤条件
     * @return 分页实体
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[sms:page]')")
    public Page<SmsMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<SmsMessageEntity> query = queryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return smsMessageSender.getSmsMessageService().findTotalPage(pageRequest, query);
    }

    /**
     * 获取短信消息
     *
     * @param id 短信消息主键 ID
     * @return 短信消息实体
     */
    @GetMapping("get")
    @PreAuthorize("hasAuthority('perms[sms:get]')")
    @Plugin(name = "编辑信息/查看明细")
    public SmsMessageEntity get(@RequestParam Integer id) {
        return smsMessageSender.getSmsMessageService().get(id);
    }

    /**
     * 删除短信消息
     *
     * @param ids 短信消息主键 ID 值集合
     */
    @PostMapping("delete")
    @PreAuthorize("hasAuthority('perms[sms:delete]')")
    @Plugin(name = "删除信息", audit = true, operationDataTrace = true)
    public RestResult<?> delete(@RequestParam List<Integer> ids) {
        smsMessageSender.getSmsMessageService().deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 获取短信余额
     *
     * @return 余额实体集合
     */
    @GetMapping("balance")
    @Plugin(name = "查看短信余额")
    @PreAuthorize("hasAuthority('perms[sms:balance]')")
    public List<SmsBalanceMeta> balance() {
        return smsMessageSender.getSmsChannelSenderList().stream().map(SmsChannelSender::getBalance).collect(Collectors.toList());
    }

    @PostMapping("send")
    @PreAuthorize("hasAuthority('perms[sms:send]')")
    @Plugin(name = "发送短信", audit = true, operationDataTrace = true)
    public RestResult<Object> send(@RequestBody SmsMessageBody body,
                                   @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        body.setUserDetails(userDetails.toBasicUserDetails());
        return smsMessageSender.sendMessage(Collections.singletonList(body));
    }

}
