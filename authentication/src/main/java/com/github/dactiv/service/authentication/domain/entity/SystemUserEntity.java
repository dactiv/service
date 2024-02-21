package com.github.dactiv.service.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.jackson.serializer.DesensitizeSerializer;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import com.github.dactiv.framework.security.enumerate.UserStatus;
import com.github.dactiv.service.authentication.domain.EmailUserDetails;
import com.github.dactiv.service.authentication.domain.PhoneNumberUserDetails;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.domain.meta.IdRoleAuthorityMeta;
import com.github.dactiv.service.commons.service.enumerate.GenderEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.*;

/**
 * 用户实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SystemUserEntity extends IntegerVersionEntity<Integer> implements EmailUserDetails {

    @Serial
    private static final long serialVersionUID = 750742816513263456L;
    /**
     * 密码字段名称
     */
    public static final String PASSWORD_FIELD_NAME = "password";

    /**
     * 最后登陆时间字段名称
     */
    public static final String LAST_AUTHENTICATION_TIME_FIELD_NAME = "lastAuthenticationTime";

    /**
     * 组细腻下
     */
    public static final String GROUPS_INFO_FIELD_NAME = "groupsInfo";

    /**
     * 邮箱
     */
    @Email
    @Length(max = 64)
    @JsonSerialize(using = DesensitizeSerializer.class)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String email;

    /**
     * 是否已验证码邮箱
     */
    private YesOrNo emailVerified = YesOrNo.No;

    /**
     * 密码
     */
    @JsonIgnore
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String password;

    /**
     * 状态:1.启用、2.禁用、3.锁定
     */
    @NotNull
    private UserStatus status;

    /**
     * 性别
     */
    private GenderEnum gender = GenderEnum.UNKNOWN;

    /**
     * 登录帐号
     */
    @Length(max = 24)
    private String username;

    /**
     * 最后认证(登录)时间
     */
    private Date lastAuthenticationTime;

    /**
     * 所属组集合
     */
    @JsonCollectionGenericType(IdRoleAuthorityMeta.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<IdRoleAuthorityMeta> groupsInfo = new LinkedList<>();

    /**
     * 独立权限资源 id 集合
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, List<String>> resourceMap = new LinkedHashMap<>();

    /**
     * 转换 security user 明细元数据
     *
     * @return map
     */
    public Map<String, Object> toSecurityUserDetailsMeta() {

        Map<String, Object> result = createBasicMeta();

        if (StringUtils.isNotBlank(email)) {
            result.put(SecurityUserDetailsConstants.EMAIL_KEY, email);
            result.put(SecurityUserDetailsConstants.EMAIL_VERIFIED_KEY, emailVerified);
        }

        if (PhoneNumberUserDetails.class.isAssignableFrom(this.getClass())) {
            PhoneNumberUserDetails userDetails = Casts.cast(this);
            result.put(SecurityUserDetailsConstants.PHONE_NUMBER_KEY, userDetails.getPhoneNumber());
            result.put(SecurityUserDetailsConstants.PHONE_NUMBER_VERIFIED_KEY, userDetails.getPhoneNumberVerified());
        }

        return result;
    }

    private Map<String, Object> createBasicMeta() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (Objects.nonNull(gender)) {
            result.put(SecurityUserDetailsConstants.GENDER_KEY, Casts.convertValue(gender, Map.class));
        }

        if (Objects.nonNull(getCreationTime())) {
            result.put(IntegerVersionEntity.CREATION_TIME_FIELD_NAME, getCreationTime());
        }

        if (Objects.nonNull(lastAuthenticationTime)) {
            result.put(SystemUserEntity.LAST_AUTHENTICATION_TIME_FIELD_NAME, lastAuthenticationTime);
        }

        return result;
    }

    public Map<String, Object> toSecurityUserDetailsMetaProfile() {
        return createBasicMeta();
    }

}
