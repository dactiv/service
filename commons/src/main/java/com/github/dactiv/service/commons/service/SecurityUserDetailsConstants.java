package com.github.dactiv.service.commons.service;

import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * spring security 用户 常量，对应 {@link  SecurityUserDetails#getMeta()} 对应的 key
 *
 * @author maurice.chen
 */
public interface SecurityUserDetailsConstants {

    String NEW_USER_KEY = "isNew";

    String USER_ID_TABLE_FIELD = "user_id";

    String USER_TYPE_TABLE_FIELD = "user_type";

    String REAL_NAME_KEY = "realName";

    String USERNAME_KEY = "username";

    String EMAIL_KEY = "email";

    String PHONE_NUMBER_KEY = "phoneNumber";

    String EMAIL_VERIFIED_KEY = "emailVerified";

    String PHONE_NUMBER_VERIFIED_KEY = "phoneNumberVerified";

    String ID_CARD_NUMBER_KEY = "idCardNumber";

    String INITIALIZATION_META_KEY = "initializationMeta";

    String GENDER_KEY = "gender";

    String JOB_NUMBER_KEY = "jobNumber";

    String MASTER_DATA_ID_KEY = "masterDataId";

    String IP_REGION_META_KEY = "";

    String REMARK_KEY = "remark";

    static <T> BasicUserDetails<T> toBasicUserDetails(SecurityUserDetails details) {
        return toBasicUserDetails(details, REAL_NAME_KEY);
    }

    static <T> BasicUserDetails<T> toBasicUserDetails(SecurityUserDetails details, String realNameKey) {

        BasicUserDetails<T> userDetails = details.toBasicUserDetails();

        if (Objects.nonNull(details.getMeta()) && details.getMeta().containsKey(realNameKey)) {
            String realName = details.getMeta().get(realNameKey).toString();

            if (StringUtils.isNotBlank(realName)) {
                userDetails.setUsername(realName);
            }
        }
        return userDetails;
    }

    static <T> void equals(TypeUserDetails<T> source, SecurityUserDetails target) {
        equals(source, target, "ID 为 [" + target.getId() + "] 的用户无法操作不属于自己的数据");
    }

    static <T> void equals(TypeUserDetails<T> source, SecurityUserDetails target, String message) {
        contains(Collections.singletonList(source), target, message);
    }

    static <T> void contains(List<TypeUserDetails<T>> sources, SecurityUserDetails target) {
        contains(sources, target, "ID 为 [" + target.getId() + "] 的用户无法操作不属于自己的数据");
    }

    static <T> void contains(List<TypeUserDetails<T>> sources, SecurityUserDetails target, String message) {
        Assert.isTrue(
                sources.stream().anyMatch(t -> t.getUserType().equals(target.getType()) && t.getUserId().equals(target.getId())),
                message
        );
    }

    static <T> void equals(TypeUserDetails<T> source, TypeUserDetails<T> target) {
        equals(source, target, "ID 为 [" + target.getUserId() + "] 的用户无法操作不属于自己的数据");
    }

    static <T> void equals(TypeUserDetails<T> source, TypeUserDetails<T> target, String message) {
        contains(Collections.singletonList(source), target, message);
    }

    static <T> void contains(List<TypeUserDetails<T>> sources, TypeUserDetails<T> target) {
        contains(sources, target, "ID 为 [" + target.getUserId() + "] 的用户无法操作不属于自己的数据");
    }

    static <T> void contains(List<TypeUserDetails<T>> sources, TypeUserDetails<T> target, String message) {
        Assert.isTrue(
                sources.stream().anyMatch(t -> t.getUserType().equals(target.getUserType()) && t.getUserId().equals(target.getUserId())),
                message
        );
    }

}
