package com.github.dactiv.service.commons.service.domain.meta;

import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.security.entity.RoleAuthority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;


/**
 * 带 id 的 RoleAuthority 实现，用于用户被分配组时存储 json 格式使用
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IdRoleAuthorityMeta extends RoleAuthority implements BasicIdentification<Integer> {

    @Serial
    private static final long serialVersionUID = 5630516721501929606L;

    /**
     * 主键 id
     */
    @NotNull
    private Integer id;

    /**
     * 是否启用
     */
    private DisabledOrEnabled status = DisabledOrEnabled.Enabled;

    public IdRoleAuthorityMeta(Integer id, String name, String authority) {
        super(name, authority);
        this.id = id;
    }

    public IdRoleAuthorityMeta(Integer id, String name, String authority, DisabledOrEnabled status) {
        this(id, name, authority);
        this.status = status;
    }

    public static IdRoleAuthorityMeta of(Integer id, String name, String authority) {
        return new IdRoleAuthorityMeta(id, name, authority);
    }

    public static IdRoleAuthorityMeta of(Integer id, String name, String authority, DisabledOrEnabled status) {
        return new IdRoleAuthorityMeta(id, name, authority, status);
    }

}
