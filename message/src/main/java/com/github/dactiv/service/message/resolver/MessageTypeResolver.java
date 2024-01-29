package com.github.dactiv.service.message.resolver;

import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;

import java.util.List;

/**
 * 消息类型解析器，用于说明某类消息有什么 {@link MessageTypeEnum} 枚举值
 *
 * @author maurice.chen
 */
public interface MessageTypeResolver {

    /**
     * 获取消息类别
     *
     * @return 消息类别
     */
    String getCategory();

    /**
     * 获取消息类型枚举集合
     *
     * @param userDetails 用户明细
     * @return 消息类型枚举集合
     */
    List<MessageTypeEnum> getMessageTypeList(TypeUserDetails<Integer> userDetails);
}
