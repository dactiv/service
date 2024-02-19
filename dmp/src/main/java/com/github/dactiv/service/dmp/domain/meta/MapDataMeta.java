package com.github.dactiv.service.dmp.domain.meta;

import com.github.dactiv.service.commons.service.domain.meta.RegionMeta;
import com.github.dactiv.service.dmp.enumerate.GatherTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.geom.Point2D;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 地图数据元数据信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class MapDataMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 2895207221456151326L;

    public static final String REGION_META_NAME = "regionMeta";

    public static final String DATA_ID_NAME = "dataId";

    public static final String DATA_IDS_NAME = "dataIds";

    /**
     * 地图数据 id
     */
    private String dataId;

    /**
     * 名称
     */
    private String name;

    /**
     * 联系方式
     */
    private List<String> contact = new ArrayList<>();

    /**
     * 类型
     */
    private List<String> type;

    /**
     * 地址
     */
    private String address;

    /**
     * 区域元数据
     */
    private RegionMeta regionMeta = new RegionMeta();

    /**
     * 点位置
     */
    private Point2D.Double point;

    /**
     * 来源地图
     */
    private GatherTypeEnum gatherType;

}
