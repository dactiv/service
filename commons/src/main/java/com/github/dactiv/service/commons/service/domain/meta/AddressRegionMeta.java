package com.github.dactiv.service.commons.service.domain.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 区域 + 代码元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddressRegionMeta extends RegionMeta {

    @Serial
    private static final long serialVersionUID = -2107580568101381048L;

    /**
     * 地址
     */
    private String address;

    private Map<String, Object> meta = new LinkedHashMap<>();
}
