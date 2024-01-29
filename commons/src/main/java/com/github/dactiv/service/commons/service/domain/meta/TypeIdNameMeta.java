package com.github.dactiv.service.commons.service.domain.meta;


import com.github.dactiv.framework.security.entity.TypeUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 带类型的 id 名称元数据
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TypeIdNameMeta extends IdNameMeta {

    public static final String TYPE_FIELD_NAME = "type";

    @Serial
    private static final long serialVersionUID = -8068875426989177482L;

    /**
     * 类型
     */
    private String type;

    public static TypeIdNameMeta of(Integer id, String name, String type) {
        TypeIdNameMeta result = new TypeIdNameMeta();

        result.setName(name);
        result.setId(id);
        result.setType(type);

        return result;
    }

    public static TypeIdNameMeta ofUserDetails(TypeUserDetails<Integer> userDetails) {
        TypeIdNameMeta result = new TypeIdNameMeta();

        result.setName(userDetails.getUsername());
        result.setId(userDetails.getUserId());
        result.setType(userDetails.getUserType());

        return result;
    }
}
