package com.github.dactiv.service.resource.domain.body;

import com.github.dactiv.framework.commons.minio.FilenameObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 发送福建到邮箱请求体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class SendAttachmentEmailRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 3434041091812732049L;

    private List<String> toEmails = new LinkedList<>();

    private List<FilenameObject> files = new LinkedList<>();
}
