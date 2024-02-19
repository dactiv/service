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
@ConfigurationProperties("dactiv.dmp.map.tencent")
public class TencentMapConfig extends MapConfigPrepareMeta {

    @Serial
    private static final long serialVersionUID = -7217084152366162396L;


    public TencentMapConfig() {
        super(GatherTypeEnum.TENCENT_MAP.getName(), GatherTypeEnum.TENCENT_MAP.getValue(), "http://localhost:8080/gather/map/icon?id=tencentMap");
    }

    /**
     * 开发密钥
     */
    private List<String> keys;

    /**
     * 当前用户对本次查找记录的存储缓存
     */
    private CacheProperties searchCache = CacheProperties.of("dactiv:dmp:map:tencent:search", TimeProperties.ofMinutes(30));

    /**
     * 获取数据缓存
     */
    private CacheProperties dataCache = CacheProperties.of("dactiv:dmp:map:tencent:data", TimeProperties.ofDay(1));
}
