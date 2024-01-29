package com.github.dactiv.service.message.service.attachment;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.minio.FileObject;

/**
 * 附件解析器
 *
 * @author maurice.chen
 */
public interface AttachmentResolver {

    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    String getMessageType();

    /**
     * 删除附件
     *
     * @param id         主键 id
     * @param fileObject 文件对象
     * @return rest 结果集
     */
    RestResult<Object> removeAttachment(Integer id, FileObject fileObject);
}
