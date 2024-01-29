package com.github.dactiv.service.message.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.commons.service.domain.meta.IdNameMeta;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import com.github.dactiv.service.message.resolver.MessageTypeResolver;
import com.github.dactiv.service.message.service.MessageSender;
import com.github.dactiv.service.message.service.attachment.AttachmentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息发送控制器
 *
 * @author maurice
 */
@RestController
@RequiredArgsConstructor
public class SystemController {

    private final List<MessageSender> messageSenders;

    private final List<MessageTypeResolver> messageTypeResolvers;

    private final List<AttachmentResolver> attachmentResolvers;

    /**
     * 获取消息类型
     *
     * @param category 类别
     * @return 带名称的 id 元数据集合
     */
    @GetMapping("getMessageType")
    @PreAuthorize("isAuthenticated()")
    public List<IdNameMeta> getMessageType(@RequestParam String category, @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        MessageTypeResolver messageTypeResolver = messageTypeResolvers
                .stream()
                .filter(m -> m.getCategory().equals(category))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到类目为 [" + category + "] 的解析器支持"));

        return messageTypeResolver
                .getMessageTypeList(userDetails.toBasicUserDetails())
                .stream()
                .map(m -> IdNameMeta.of(m.getValue(), m.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 发送消息
     *
     * @param body http servlet request body
     * @return 消息结果集
     */
    @PreAuthorize("hasRole('BASIC')")
    @PostMapping(value = "send", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Plugin(name = "发送消息", id = "send", parent = "message")
    public RestResult<Object> send(@RequestBody Map<String, Object> body,
                                   @CurrentSecurityContext SecurityContext securityContext) throws Exception {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        Map<String, Object> user = Casts.convertValue(userDetails.toBasicUserDetails(), Casts.MAP_TYPE_REFERENCE);
        String type = body.get(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY).toString();
        body.putAll(user);
        return getMessageService(type).send(body);
    }

    @PostMapping(value = "removeAttachment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RestResult<Object> removeAttachment(@RequestParam Integer id, @RequestParam String type, @RequestBody FileObject fileObject) {
        return getAttachmentResolver(type).removeAttachment(id, fileObject);
    }

    /**
     * 更具类型获取验证码服务
     *
     * @param type 消息类型
     * @return 验证码服务
     */
    private MessageSender getMessageService(String type) {
        return messageSenders
                .stream()
                .filter(c -> c.getMessageType().equals(type))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到类型为[ " + type + " ]的消息发送服务"));
    }

    private AttachmentResolver getAttachmentResolver(String type) {
        return attachmentResolvers
                .stream()
                .filter(c -> c.getMessageType().equals(type))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到类型为[ " + type + " ]的附件解析器"));
    }

}
