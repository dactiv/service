package com.github.dactiv.service.commons.service.domain.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.service.commons.service.enumerate.ValueTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 资源字典元数据信息
 *
 * @author maurice.chen
 */

@Data
public class ResourceDictionaryMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -8621982665173979070L;

    /**
     * 名称
     */
    @NotNull
    @Length(max = 64)
    private String name;

    /**
     * 值
     */
    @NotNull
    private Object value;

    /**
     * 键名称
     */
    @NotEmpty
    @Length(max = 256)
    private String code;

    /**
     * 值类型
     */
    @NotNull
    @JsonIgnore
    private ValueTypeEnum valueType;

    /**
     * 等级
     */
    private String level;

}
