package com.github.dactiv.service.dmp.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.dmp")
public class ApplicationConfig {

    public static final String SEARCH_ID_FIELD_NAME = "searchId";

    private String mapIconPath = "./avatar/map";

    /**
     * 同步 es 数据的 mapping 模版路径
     */
    private String elasticsearchMappingPath = "elasticsearch";
}
