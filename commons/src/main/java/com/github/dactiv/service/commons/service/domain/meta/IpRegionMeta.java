package com.github.dactiv.service.commons.service.domain.meta;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * ip 区域原数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class IpRegionMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -357706294703499044L;

    public static final String IP_ADDRESS_NAME = "ipAddress";

    /**
     * ip 地址
     */
    @NonNull
    @NotEmpty
    private String ipAddress;

    /**
     * 区域信息
     */
    private RegionMeta regionMeta;

}
