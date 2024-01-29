package com.github.dactiv.service.authentication.plugin;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Map;

/**
 * 插件实例
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PluginInstance extends Instance {

    @Serial
    private static final long serialVersionUID = 6418529914611005984L;

    /**
     * 版本号
     */
    private Version version;

    /**
     * 分组信息
     */
    private String group;

    /**
     * info 信息
     */
    private Map<String, Object> info;

}
