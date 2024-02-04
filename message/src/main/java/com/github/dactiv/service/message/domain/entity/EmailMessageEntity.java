package com.github.dactiv.service.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.minio.FilenameObject;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.service.message.domain.AttachmentMessage;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>邮件消息实体类</p>
 * <p>Table: tb_email_message - 邮件消息</p>
 *
 * @author maurice
 * @since 2020-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("emailMessage")
@TableName(value = "tb_email_message", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class EmailMessageEntity extends BasicMessageEntity implements AttachmentMessage, BatchMessageEntity.Body, TypeUserDetails<Integer> {

    @Serial
    private static final long serialVersionUID = 8360029094205090328L;

    /**
     * 标题
     */
    private String title;

    /**
     * 发送邮件
     */
    @NotNull
    private String fromEmail;

    /**
     * 收取邮件
     */
    private String toEmail;

    /**
     * 最后发送时间
     */
    private Date lastSendTime;

    /**
     * 批量消息 id
     */
    private Integer batchId;

    /**
     * 附件集合
     */
    @JsonCollectionGenericType(FilenameObject.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<FilenameObject> attachmentList = new ArrayList<>();

    private String username;

    private Integer userId;

    private String userType;

}