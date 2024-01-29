package com.github.dactiv.service.commons.service.domain.meta;

import com.github.dactiv.framework.commons.Casts;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 区域元数据
 *
 * @author maurice.chen
 */
@Data
public class RegionMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 3470046783490160867L;

    public static final String SIMPLE_NAME = "region";

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区域
     */
    private String district;

    /**
     * 国家
     */
    private String country;

    /**
     * 行政区域代码
     */
    private String adCode;

    /**
     * 国家代码
     */
    private String countryCode;

    /**
     * 城市代码
     */
    private String cityCode;

    public String getRegionString() {
        return StringUtils.join(Stream.of(country, province, city, district).filter(Objects::nonNull).collect(Collectors.toList()), Casts.COMMA);
    }

    public String getShortRegionString() {
        return StringUtils.join(Stream.of(province, city).filter(Objects::nonNull).collect(Collectors.toList()), Casts.COMMA);
    }

}
