package com.github.dactiv.service.commons.service.domain.meta;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * es 同步元数据信息
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class ElasticsearchSyncMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -6292789010043241331L;

    /**
     * 主键 id
     */
    private String id;

    /**
     * 索引值
     */
    private String indexName;

    /**
     * 数据信息
     */
    private Map<String, Object> object;

}
