package com.github.dactiv.service.authentication.config;

import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 异地登录配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties("dactiv.service.authentication.abnormal-area")
public class AbnormalAreaConfig {

    /**
     * 发送站内信的消息内容
     */
    private String sendContent = "您的账户在异地登录，如果非本人操作。请及时修改密码。";

    /**
     * 发送站内信的标题内容
     */
    private String title = "异地登录通知";

    /**
     * 消息类型
     */
    private MessageTypeEnum messageType = MessageTypeEnum.WARNING;

    /**
     * 发送类型, siteMessage 为站内信
     */
    private String sendType = "site";

    /**
     * 发送失败后的重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 坐标点换取省市县的地图类型
     */
    private String locationMapType = "alibabaMap";

    /**
     * 是否同步会员用户位置
     */
    private boolean isSyncMemberUserLocation = true;
}
