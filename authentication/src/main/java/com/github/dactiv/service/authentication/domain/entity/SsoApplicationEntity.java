package com.github.dactiv.service.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import com.github.dactiv.service.authentication.enumerate.SsoApplicationEnvironmentEnum;
import com.github.dactiv.service.authentication.enumerate.SsoApplicationTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * <p>Table: tb_sso_application - 单点登陆应用</p>
 *
 * @author maurice.chen
 * @since 2023-11-29 10:22:33
 */
@Data
@Alias("ssoApplication")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_sso_application", autoResultMap = true)
public class SsoApplicationEntity extends IntegerVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 3733013539719504448L;

    /**
     * 商户客户端 id
     */
    @NotNull
    private String merchantClientId;

    /**
     * app id
     *
     * @deprecated 旧系统需要，兼容完新系统后删除该字段
     */
    @Deprecated
    private String appKey;

    /**
     * app 密钥
     *
     * @deprecated 旧系统需要，兼容完新系统后删除该字段
     */
    @Deprecated
    private String appSecret;

    /**
     * icon 图标
     */
    private String icon;

    /**
     * 名称
     */
    @NotNull
    private String name;

    /**
     * 应用链接
     */
    @NotNull
    private String url;

    /**
     * 应用环境
     */
    @NotNull
    private SsoApplicationEnvironmentEnum environment;

    /**
     * 应用类型
     */
    @NotNull
    private SsoApplicationTypeEnum type;

    /**
     * 是否启用
     */
    private YesOrNo enabled = YesOrNo.Yes;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> meta = new LinkedHashMap<>();

    /**
     * 备注
     */
    private String remark;

}