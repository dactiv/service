package com.github.dactiv.service.dmp.resolver;

import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.service.dmp.domain.meta.MapConfigPrepareMeta;
import com.github.dactiv.service.dmp.domain.meta.MapDataMeta;
import com.github.dactiv.service.dmp.enumerate.GatherTypeEnum;

import java.util.List;

/**
 * 地图数据缓存解析器
 *
 * @author maurice.chen
 */
public interface MapResolver {

    /**
     * 查询数据
     *
     * @param userDetails 用户明细
     * @param searchId    查询数据 id
     * @param request     分页信息
     * @param keyword     关键字
     * @param city        城市
     * @return 地图数据分页对象
     */
    Page<MapDataMeta> search(TypeUserDetails<Integer> userDetails,
                             String searchId,
                             PageRequest request,
                             String keyword,
                             List<String> city);

    /**
     * 获取地图数据元数据集合
     *
     * @param userDetails 用户明细
     * @param searchId    查询 id
     * @return 地图数据元数据集合
     */
    List<MapDataMeta> getUserMapDataMetas(TypeUserDetails<Integer> userDetails, String searchId);

    /**
     * 解析器名称
     *
     * @return 解析器名称
     */
    MapConfigPrepareMeta getDetails();

    /**
     * 删除 缓存数据
     *
     * @param userDetails 用户明细
     * @param searchId 查询 id
     */
    void deleteUserSearchCache(TypeUserDetails<Integer> userDetails, String searchId);

    /**
     * 转换地图数据元数据信息
     *
     * @param data 地图数据元数据信息集合
     * @param userDetails 当前用户
     *
     * @return 转换后的地图数据元数据信息集合
     */
    List<MapDataMeta> convertMapDataMeta(List<MapDataMeta> data, TypeUserDetails<Integer> userDetails);
    /**
     * 获取地图类型
     *
     * @return 地图类型
     */
    GatherTypeEnum getType();

    /**
     * 根据 search ID 创建查询内容
     *
     * @param userDetails 当前用户
     * @param searchId 查询 id
     */
    void createBySearchId(TypeUserDetails<Integer> userDetails, String searchId);
}
