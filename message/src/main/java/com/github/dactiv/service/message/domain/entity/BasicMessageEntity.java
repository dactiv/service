package com.github.dactiv.service.message.domain.entity;

import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.retry.Retryable;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Date;

/**
 * 基础消息实体，用于将所有消息内容公有化使用。
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BasicMessageEntity extends IntegerVersionEntity<Integer> implements Retryable, ExecuteStatus.Body {

    @Serial
    private static final long serialVersionUID = -1167940666968537341L;

    /**
     * 类型
     *
     * @see MessageTypeEnum
     */
    @NotNull
    private MessageTypeEnum type;

    /**
     * 内容
     */
    private String content;


    /**
     * 重试次数
     */
    private Integer retryCount = 1;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 发送成功时间
     */
    private Date successTime;

    /**
     * 状态：0.执行中、1.执行成功，2.重试中，99.执行失败
     *
     * @see ExecuteStatus
     */
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

    /**
     * 备注
     */
    private String remark;

}
