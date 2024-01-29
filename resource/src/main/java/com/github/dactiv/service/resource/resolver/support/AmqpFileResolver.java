package com.github.dactiv.service.resource.resolver.support;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.commons.service.domain.meta.TypeIdNameMeta;
import com.github.dactiv.service.commons.service.enumerate.AttachmentTypeEnum;
import com.github.dactiv.service.commons.service.enumerate.OssTypeEnum;
import com.github.dactiv.service.resource.config.AttachmentConfig;
import com.github.dactiv.service.resource.resolver.AttachmentResolver;
import io.minio.ObjectWriteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * rocketmq 文件发送解析器
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AmqpFileResolver implements AttachmentResolver {

    private final AmqpTemplate amqpTemplate;

    @Override
    public boolean isSupport(AttachmentTypeEnum attachmentType) {
        return AttachmentTypeEnum.USER_FILE.equals(attachmentType);
    }

    @Override
    public void postUpload(MultipartFile file,
                           Map<String, Object> result,
                           ObjectWriteResponse response,
                           SecurityUserDetails userDetails,
                           Map<String, String> userMetadata,
                           Map<String, Object> appendParam) {

        Map<String, Object> message = new LinkedHashMap<>(appendParam);

        String exchange = message.getOrDefault(AttachmentConfig.RABBIT_MQ_EXCHANGE_NAME, StringUtils.EMPTY).toString();
        Assert.hasText(exchange, "exchange 参数不能为空");

        String routingKey = message.getOrDefault(AttachmentConfig.RABBIT_MQ_ROUTING_KEY_NAME, StringUtils.EMPTY).toString();
        Assert.hasText(routingKey, "routingKey 参数不能为空");

        message.put(TypeIdNameMeta.TYPE_FIELD_NAME, OssTypeEnum.MINIO.getValue());
        result.put(RestResult.DEFAULT_MESSAGE_NAME, message);

        amqpTemplate.convertAndSend(exchange, routingKey, result);
        AttachmentResolver.super.postUpload(file, result, response, userDetails, userMetadata, appendParam);
    }
}
