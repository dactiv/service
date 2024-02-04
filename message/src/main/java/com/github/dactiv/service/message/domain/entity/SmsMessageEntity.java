package com.github.dactiv.service.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>短信消息实体类</p>
 * <p>Table: tb_sms_message - 短信消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("smsMessage")
@TableName(value = "tb_sms_message", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class SmsMessageEntity extends BasicMessageEntity implements BatchMessageEntity.Body, TypeUserDetails<Integer> {

    @Serial
    private static final long serialVersionUID = 3229810529789017287L;

    /**
     * 渠道名称
     */
    private String channel;

    /**
     * 手机号码
     */
    private String phoneNumber;

    /**
     * 批量消息 id
     */
    private Integer batchId;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> meta = new LinkedHashMap<>();

    /**
     * 发送用户 id
     */
    private Integer userId;

    /**
     * 发送用户登陆账号
     */
    private String username;

    /**
     * 发送用户账号类型
     */
    private String userType;

}