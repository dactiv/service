package com.github.dactiv.service.commons.service.domain.meta;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.awt.geom.Point2D;
import java.io.Serial;
import java.util.Objects;

/**
 * 带位置的 ip 区域元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LocationIpRegionMeta extends IpRegionMeta {

    @Serial
    private static final long serialVersionUID = -901855885374236004L;

    /**
     * 位置
     */
    private Point2D.Double location;

    /**
     * 是否行政区域代码错误
     *
     * @return true 是，否则 false
     */
    public boolean isAdCodeError() {
        if (Objects.isNull(location)) {
            return false;
        }

        return Objects.isNull(getRegionMeta()) || StringUtils.isEmpty(getRegionMeta().getAdCode());
    }
}
