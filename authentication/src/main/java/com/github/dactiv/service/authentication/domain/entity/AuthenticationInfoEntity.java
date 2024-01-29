package com.github.dactiv.service.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.service.authentication.domain.IpAddressDetails;
import com.github.dactiv.service.commons.service.domain.meta.LocationIpRegionMeta;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;
import java.util.Map;

/**
 * <p>认证信息实体类</p>
 * <p>Table: tb_authentication_info - 认证信息表</p>
 *
 * @author maurice
 * @since 2020-06-01 09:22:12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_authentication_info", autoResultMap = true)
public class AuthenticationInfoEntity extends BasicUserDetails<Integer> implements BasicIdentification<String>, IpAddressDetails {

    public static final String ELASTICSEARCH_INDEX_NAME = "ix_authentication_info";

    @Serial
    private static final long serialVersionUID = 5548079224380108843L;

    private String id;

    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 用户元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> meta;

    /**
     * ip 地址
     */
    @NotEmpty
    @EqualsAndHashCode.Exclude
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private LocationIpRegionMeta ipRegionMeta;

    /**
     * 设备名称
     */
    @NotEmpty
    @EqualsAndHashCode.Exclude
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, String> device;

    /**
     * 备注
     */
    @EqualsAndHashCode.Exclude
    private String remark;
}