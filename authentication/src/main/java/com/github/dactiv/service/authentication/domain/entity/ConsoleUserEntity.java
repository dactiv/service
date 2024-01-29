package com.github.dactiv.service.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.jackson.serializer.DesensitizeSerializer;
import com.github.dactiv.service.authentication.domain.PhoneNumberUserDetails;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.SystemConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.Map;

/**
 * <p>系统用户实体类</p>
 * <p>Table: tb_console_user - 系统用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@Alias("consoleUser")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_console_user", autoResultMap = true)
public class ConsoleUserEntity extends SystemUserEntity implements PhoneNumberUserDetails {

    @Serial
    private static final long serialVersionUID = 1815468583503444307L;
    /**
     * 真实姓名
     */
    @NotEmpty
    @Length(max = 16)
    private String realName;

    /**
     * 联系电话
     */
    @Length(max = 32)
    @JsonSerialize(using = DesensitizeSerializer.class)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Pattern(regexp = SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION)
    private String phoneNumber;

    /**
     * 是否已验证码联系电话
     */
    private YesOrNo phoneNumberVerified;

    /**
     * 备注
     */
    private String remark;

    @Override
    public Map<String, Object> toSecurityUserDetailsMeta() {
        Map<String, Object> result = super.toSecurityUserDetailsMeta();

        if (StringUtils.isNotBlank(realName)) {
            result.put(SecurityUserDetailsConstants.REAL_NAME_KEY, realName);
        }

        if (StringUtils.isNotBlank(remark)) {
            result.put(SecurityUserDetailsConstants.REMARK_KEY, remark);
        }

        return result;
    }

    @Override
    public Map<String, Object> toSecurityUserDetailsMetaProfile() {
        Map<String, Object> result = super.toSecurityUserDetailsMetaProfile();

        if (StringUtils.isNotBlank(realName)) {
            result.put(SecurityUserDetailsConstants.REAL_NAME_KEY, realName);
        }

        if (StringUtils.isNotBlank(remark)) {
            result.put(SecurityUserDetailsConstants.REMARK_KEY, remark);
        }

        return result;
    }
}