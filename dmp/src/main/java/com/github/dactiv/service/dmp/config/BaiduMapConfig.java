package com.github.dactiv.service.dmp.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.service.dmp.domain.meta.MapConfigPrepareMeta;
import com.github.dactiv.service.dmp.enumerate.GatherTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.util.List;

/**
 * 地图数据采集配置
 *
 * @author maurice.chen
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("dactiv.dmp.map.baidu")
public class BaiduMapConfig extends MapConfigPrepareMeta {


    @Serial
    private static final long serialVersionUID = 8623920789533884120L;

    public BaiduMapConfig() {
        super(GatherTypeEnum.BAIDU_MAP.getName(), GatherTypeEnum.BAIDU_MAP.getValue(), "http://localhost:8080/gather/map/icon?id=baiduMap");
    }

    /**
     * 开发密钥
     */
    private List<String> keys;

    /**
     * 区域数据召回限制，为true时，仅召回 region 对应区域内数据
     */
    private boolean cityLimit = true;

    /**
     * 是否召回国标行政区划编码，true（召回）、false（不召回）
     */
    private boolean extensionsAdCode = true;

    /**
     * 当前用户对本次查找记录的存储缓存
     */
    private CacheProperties searchCache = CacheProperties.of("dactiv:dmp:map:baidu:search", TimeProperties.ofMinutes(30));

    /**
     * 获取数据缓存
     */
    private CacheProperties dataCache = CacheProperties.of("dactiv:dmp:map:baidu:data", TimeProperties.ofDay(1));
}
