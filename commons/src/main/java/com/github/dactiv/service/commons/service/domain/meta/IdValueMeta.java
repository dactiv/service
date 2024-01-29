package com.github.dactiv.service.commons.service.domain.meta;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 带值的 id 实体
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IdValueMeta<T, V> extends IdEntity<T> {

    @Serial
    private static final long serialVersionUID = -8885126404039341575L;

    public static final String VALUE_FIELD_NAME = "value";

    /**
     * 值
     */
    private V value;

    public IdValueMeta() {
    }

    public IdValueMeta(T id, V value) {
        super(id);
        this.value = value;
    }

    public static <T, V> IdValueMeta<T, V> of(T id, V value) {
        IdValueMeta<T, V> result = new IdValueMeta<>();
        result.setId(id);
        result.setValue(value);

        return result;
    }

    public static <T, V> Object ofMap(Map<T, V> data, boolean idValueFormat) {
        if (idValueFormat) {
            List<IdValueMeta<T, V>> listValue = new LinkedList<>();
            data.forEach((k, v) -> listValue.add(IdValueMeta.of(k, v)));
            return listValue;
        }

        return data;
    }

    public static <T, V> Object ofList(List<IdValueMeta<T, V>> data, boolean idValueFormat) {
        if (idValueFormat) {
            return data.stream().map(d -> Casts.of(d, IdValueMeta.class)).collect(Collectors.toList());
        }

        Map<T, V> result = new LinkedHashMap<>();
        data.forEach(meta -> result.put(meta.getId(), meta.getValue()));

        return result;
    }
}
