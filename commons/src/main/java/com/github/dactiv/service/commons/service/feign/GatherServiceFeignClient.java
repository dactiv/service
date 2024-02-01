package com.github.dactiv.service.commons.service.feign;


import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.domain.meta.AddressRegionMeta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.geom.Point2D;

/**
 * 采集服务 feign 客户端
 *
 * @author maurice.chen
 */
@FeignClient(value = SystemConstants.SYS_GATHER_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface GatherServiceFeignClient {

    /**
     * 获取地图区域信息
     *
     * @param point 坐标点
     * @param type 地图类型
     *
     * @return 区域信息
     */
    @PostMapping("map/getMapRegion")
    AddressRegionMeta getMapRegion(@RequestBody Point2D.Double point, @RequestParam("type") String type);
}
