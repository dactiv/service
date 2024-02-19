package com.github.dactiv.service.dmp.domain.meta;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 地图详情元数据信息
 *
 * @author maurice.chen
 */
@Data
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class MapConfigPrepareMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 5074584287076634777L;

    /**
     * 地图名称
     */
    @NonNull
    private String name;

    /**
     * 地图 id
     */
    @NonNull
    private String id;

    /**
     * 地图图标
     */
    @NonNull
    private String icon;

    /**
     * 是否启用，false.否，true.是
     */
    private boolean enable = true;

    /**
     * js 版本的密钥信息
     */
    private String jsKey;

}
