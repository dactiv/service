package com.github.dactiv.service.authentication.plugin;

import com.github.dactiv.framework.nacos.event.NacosService;
import com.github.dactiv.service.authentication.domain.meta.ResourceMeta;

import java.util.List;

/**
 * 插件资源拦截器
 *
 * @author maurice.chen
 */
public interface PluginResourceInterceptor {

    /**
     * 同步插件完成后的处理
     *
     * @param instance        插件实例
     * @param newResourceList 插件新资源集合
     */
    void postSyncPlugin(PluginInstance instance, List<ResourceMeta> newResourceList);

    /**
     * 禁用应用资源后的处理
     *
     * @param nacosService nacos 服务
     */
    default void postDisabledApplicationResource(NacosService nacosService) {

    }
}
