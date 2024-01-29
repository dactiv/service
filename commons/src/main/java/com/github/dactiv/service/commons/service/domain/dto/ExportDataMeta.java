package com.github.dactiv.service.commons.service.domain.dto;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.retry.Retryable;
import com.github.dactiv.framework.security.entity.BasicUserDetails;
import com.github.dactiv.service.commons.service.enumerate.ImportExportTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.util.AntPathMatcher;

import java.io.Serial;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 导出数据模型
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExportDataMeta extends BasicUserDetails<Integer> implements BasicIdentification<String>, ExecuteStatus.Body, Retryable {

    @Serial
    private static final long serialVersionUID = 8006955473517765144L;

    public static final String SIMPLE_NAME = "export";

    /**
     * 主键 id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 状态
     */
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 成功时间
     */
    private Date successTime;

    /**
     * 最后导出时间
     */
    private Date lastExportTime = new Date();

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 导出类型
     */
    private ImportExportTypeEnum type;

    /**
     * 文件大小
     */
    private long size = 0;

    /**
     * 元数据信息
     */
    private Map<String, Object> meta = new LinkedHashMap<>();

    public String toExportCacheName() {
        return getUserId()
                + RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE
                + getUserType()
                + CacheProperties.DEFAULT_SEPARATOR
                + getType().getValue()
                + CacheProperties.DEFAULT_SEPARATOR
                + getId();
    }

    public String toUploadFilename() {
        return getUserType() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + getUserId() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + getFilename();
    }
}
