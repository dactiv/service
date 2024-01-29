package com.github.dactiv.service.commons.service.domain.meta;

import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.enumerate.NameEnum;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.id.IdEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Objects;

/**
 * 带名称的 id 元数据
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IdNameMeta extends IdEntity<Integer> {

    public static final String NAME_FIELD_NAME = "name";

    @Serial
    private static final long serialVersionUID = -4127420686530448230L;

    /**
     * 名称
     */
    @NotNull
    private String name;

    public static IdNameMeta of(Integer id, String name) {
        IdNameMeta result = new IdNameMeta();
        result.setId(id);
        result.setName(name);
        return result;
    }

    public static IdNameMeta of(BasicIdentification<Integer> source) {
        return of(source, NameEnum.FIELD_NAME);
    }

    public static IdNameMeta of(BasicIdentification<Integer> source, String nameProperty) {
        IdNameMeta result = new IdNameMeta();
        result.setId(source.getId());

        Object nameValue = ReflectionUtils.getReadProperty(source, nameProperty);

        if (Objects.isNull(nameValue)) {
            nameValue = ReflectionUtils.getFieldValue(source, nameProperty);
        }

        if (Objects.nonNull(nameValue)) {
            result.setName(nameValue.toString());
        }

        return result;
    }
}
