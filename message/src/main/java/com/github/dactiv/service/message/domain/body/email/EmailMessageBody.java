package com.github.dactiv.service.message.domain.body.email;

import com.github.dactiv.framework.commons.minio.FilenameObject;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.service.message.domain.AttachmentMessage;
import com.github.dactiv.service.message.domain.entity.BasicMessageEntity;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 邮件消息 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailMessageBody extends BasicMessageEntity implements AttachmentMessage, TypeUserDetails<Integer> {

    @Serial
    private static final long serialVersionUID = -1367698344075208239L;

    /**
     * 标题
     */
    private String title;

    /**
     * 发送用户 id
     */
    private Integer userId;

    /**
     * 发送用户名
     */
    private String username;

    /**
     * 发送用户类型
     */
    private String userType;

    /**
     * 收件方集合
     */
    @NotEmpty
    private List<String> toEmails = new LinkedList<>();

    /**
     * 附件
     */
    private List<FilenameObject> attachmentList = new ArrayList<>();

}
