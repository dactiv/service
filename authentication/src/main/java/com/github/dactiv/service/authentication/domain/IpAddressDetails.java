package com.github.dactiv.service.authentication.domain;

import com.github.dactiv.service.commons.service.domain.meta.LocationIpRegionMeta;

/**
 * ip 地址实体
 *
 * @author maurice.chen
 */
public interface IpAddressDetails {

    /**
     * 获取 ip 区域元数据信息
     *
     * @return ip 区域元数据信息
     */
    LocationIpRegionMeta getIpRegionMeta();

    /**
     * 设置 ip 区域元数据信息
     *
     * @param ipRegionMeta ip 区域元数据信息
     */
    void setIpRegionMeta(LocationIpRegionMeta ipRegionMeta);
}
