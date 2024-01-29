package com.github.dactiv.service.commons.service.feign;


import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.domain.meta.AddressRegionMeta;
import org.springframework.cloud.openfeign.FeignClient;

import java.awt.geom.Point2D;

/**
 * 采集服务 feign 客户端
 *
 * @author maurice.chen
 */
@FeignClient(value = SystemConstants.SYS_AUTHENTICATION_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface GatherServiceFeignClient {

    AddressRegionMeta getMapRegion(Point2D.Double location, String locationMapType);
}
