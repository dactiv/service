package com.github.dactiv.service.message.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.service.message.enumerate.AttachmentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;


/**
 * <p>批量消息实体类</p>
 * <p>Table: tb_batch_message - 批量消息</p>
 *
 * @author maurice
 * @since 2021-08-22 04:45:14
 */
@Data
@Alias("batchMessage")
@TableName("tb_batch_message")
@EqualsAndHashCode(callSuper = true)
public class BatchMessageEntity extends IdEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 3580346090724641812L;

    /**
     * 主键
     */
    private Integer id;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 更新版本号
     */
    @Version
    private Integer version;

    /**
     * 完成时间
     */
    private Date completeTime;

    /**
     * 状态:0.执行中、1.执行成功，99.执行失败
     */
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

    /**
     * 总数
     */
    private Integer count = 0;

    /**
     * 成功发送数量
     */
    private Integer successNumber = 0;

    /**
     * 失败发送数量
     */
    private Integer failNumber = 0;

    /**
     * 类型:10.站内信,20.邮件,30.短信
     */
    private AttachmentTypeEnum type;

    /**
     * 获取发送重的数量
     *
     * @return 数量
     */
    public Integer getSendingNumber() {
        return count - successNumber - failNumber;
    }

    /**
     * 批量消息接口，用于统一规范使用
     *
     * @author maurice.chen
     */
    public interface Body extends BasicIdentification<Integer> {

        /**
         * 获取批量消息 id
         *
         * @return 批量消息 id
         */
        Integer getBatchId();

        /**
         * 设置批量消息 id
         *
         * @param batchId 批量消息 id
         */
        void setBatchId(Integer batchId);

        /**
         * 获取状态
         *
         * @return 执行状态
         */
        ExecuteStatus getExecuteStatus();
    }

}