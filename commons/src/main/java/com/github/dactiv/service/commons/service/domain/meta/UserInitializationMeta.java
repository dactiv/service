package com.github.dactiv.service.commons.service.domain.meta;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户初始化元数据信息
 *
 * @author maurice.chen
 */

@Data
public class UserInitializationMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -8419236109054403628L;

    /**
     * 是否可更新密码：1.是、0.否
     */
    @NotNull
    private YesOrNo randomPassword = YesOrNo.Yes;

    /**
     * 是否可更新登录账户：1.是、0.否
     */
    @NotNull
    private YesOrNo randomUsername = YesOrNo.Yes;
}
