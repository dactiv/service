package com.github.dactiv.service.message.domain;

import com.github.dactiv.framework.commons.minio.FilenameObject;

import java.util.List;

/**
 * 带附件的消息
 *
 * @author maurice.chen
 */
public interface AttachmentMessage {

    String ATTACHMENT_LIST_FIELD_NAME = "attachmentList";

    /**
     * 获取附件信息集合
     *
     * @return 附件信息集合
     */
    List<FilenameObject> getAttachmentList();
}
