package com.github.dactiv.service.dmp.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.dmp.domain.meta.MapConfigPrepareMeta;
import com.github.dactiv.service.dmp.domain.meta.MapDataMeta;
import com.github.dactiv.service.dmp.enumerate.GatherTypeEnum;
import com.github.dactiv.service.dmp.service.MapGatherService;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 地图客源采集
 *
 * @author maurice.chen
 */
@RestController
@RequestMapping("map")
public class MapController {

    private final MapGatherService mapGatherService;

    public MapController(MapGatherService mapGatherService) {
        this.mapGatherService = mapGatherService;
    }

    /**
     * 高德地图客源采集
     *
     * @param pageRequest 分页信息
     * @param keyword 关键字
     * @param city 采集城市
     *
     * @return 对应的地图数据
     */
    @Plugin(name = "高德地图客源采集")
    @PostMapping("alibabaMapSearch")
    @PreAuthorize("hasAuthority('perms[map:alibaba_map_search]')")
    public Page<MapDataMeta> aliBabaMapSearch(PageRequest pageRequest,
                                              @CurrentSecurityContext SecurityContext securityContext,
                                              @RequestParam String searchId,
                                              @RequestParam String keyword,
                                              @RequestParam List<String> city) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        return mapGatherService.mapSearch(
                userDetails.toBasicUserDetails(),
                GatherTypeEnum.ALIBABA_MAP,
                searchId,
                pageRequest,
                keyword,
                city
        );
    }

    /**
     * 获取高德地图坐标
     *
     * @param address 地址
     * @param city 所在城市
     *
     * @return 坐标值
     */
    @GetMapping("getAlibabaMapPoint")
    @PreAuthorize("isAuthenticated()")
    public Point2D.Double getAlibabaMapPoint(@RequestParam String address, @RequestParam(required = false) String city) {
        return mapGatherService.getAlibabaMapPoint(address, city);
    }

    /**
     * 腾讯地图客源采集
     *
     * @param pageRequest 分页信息
     * @param keyword 关键字
     * @param city 采集城市
     *
     * @return 对应的地图数据
     */
    @Plugin(name = "腾讯地图客源采集")
    @PostMapping("tencentMapSearch")
    @PreAuthorize("hasAuthority('perms[map:tencent_map_search]')")
    public Page<MapDataMeta> tencentMapSearch(PageRequest pageRequest,
                                              @CurrentSecurityContext SecurityContext securityContext,
                                              @RequestParam String searchId,
                                              @RequestParam String keyword,
                                              @RequestParam List<String> city) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        return mapGatherService.mapSearch(
                userDetails.toBasicUserDetails(),
                GatherTypeEnum.TENCENT_MAP,
                searchId,
                pageRequest,
                keyword,
                city
        );
    }

    /**
     * 百度地图客源采集
     *
     * @param pageRequest 分页信息
     * @param keyword 关键字
     * @param city 采集城市
     *
     * @return 对应的地图数据
     */
    @Plugin(name = "百度地图客源采集")
    @PostMapping("baiduMapSearch")
    @PreAuthorize("hasAuthority('perms[map:baidu_map_search]')")
    public Page<MapDataMeta> baiduMapSearch(PageRequest pageRequest,
                                            @CurrentSecurityContext SecurityContext securityContext,
                                            @RequestParam String searchId,
                                            @RequestParam String keyword,
                                            @RequestParam List<String> city) {

        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        return mapGatherService.mapSearch(
                userDetails.toBasicUserDetails(),
                GatherTypeEnum.BAIDU_MAP,
                searchId,
                pageRequest,
                keyword,
                city
        );
    }

    /**
     * 获取查询 id
     *
     * @return 查询 id
     */
    @PostMapping("getSearchId")
    @PreAuthorize("hasAnyAuthority('perms[map:baidu_map_search]','perms[map:tencent_map_search]','perms[map:alibaba_map_search]')")
    public RestResult<String> getSearchId(@CurrentSecurityContext SecurityContext securityContext, @RequestParam List<String> types) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        List<GatherTypeEnum> gatherType = types
                .stream()
                .map(t -> ValueEnumUtils.parse(t, GatherTypeEnum.class))
                .collect(Collectors.toList());
        String id = mapGatherService.generateSearchId(userDetails.toBasicUserDetails(), gatherType);
        return RestResult.ofSuccess(id);
    }

    /**
     * 获取当前地图采集数据
     *
     * @param searchId 地图查询请求体
     * @param securityContext spring 安全上下文
     *
     * @return 当前地图采集数据集合
     */
    @Plugin(name = "获取当前地图采集数据")
    @GetMapping("getCurrentMapGatherData")
    @PreAuthorize("hasAnyAuthority('perms[dmp_map:get_current_map_gather_data]')")
    public Map<String, List<MapDataMeta>> getCurrentMapGatherData(@RequestParam String searchId,
                                                                  @CurrentSecurityContext SecurityContext securityContext) {
        SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        return mapGatherService.getSearchData(userDetails.toBasicUserDetails(), searchId);
    }

    @GetMapping("prepare")
    public List<MapConfigPrepareMeta> prepare() {
        return mapGatherService.getMapDetails();
    }

    @GetMapping("icon")
    public ResponseEntity<byte[]> icon(String id) throws Exception {
        InputStream is = mapGatherService.getMapIcon(id);
        ResponseEntity<byte[]> result = new ResponseEntity<>(IOUtils.toByteArray(is), new HttpHeaders(), HttpStatus.OK);
        IOUtils.close(is);
        return result;
    }
}
