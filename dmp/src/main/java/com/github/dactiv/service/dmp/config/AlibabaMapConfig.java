package com.github.dactiv.service.dmp.config;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.service.commons.service.domain.meta.CloudSecurityMeta;
import com.github.dactiv.service.dmp.domain.meta.MapConfigPrepareMeta;
import com.github.dactiv.service.dmp.enumerate.GatherTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.util.List;

/**
 * 高德地图数据采集配置
 *
 * @author maurice.chen
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("dactiv.dmp.map.alibaba")
public class AlibabaMapConfig extends MapConfigPrepareMeta {

    @Serial
    private static final long serialVersionUID = -1301848893706246546L;


    public AlibabaMapConfig() {
        super(GatherTypeEnum.ALIBABA_MAP.getName(), GatherTypeEnum.ALIBABA_MAP.getValue(), "http://localhost:8080/gather/map/icon?id=alibabaMap");
    }

    /**
     * 密钥信息
     */
    private List<CloudSecurityMeta> cloudSecurityMetas;

    /**
     * 是否强制限制成功
     */
    private boolean cityLimit = true;

    /**
     * 查询数据缓存
     */
    private CacheProperties searchCache = CacheProperties.of("cloudmasses.saas.gather:map:alibaba:search", TimeProperties.ofMinutes(30));

    /**
     * 获取数据缓存
     */
    private CacheProperties dataCache = CacheProperties.of("cloudmasses.saas.gather:map:alibaba:data", TimeProperties.ofDay(1));
}
