package com.github.dactiv.service.resource.domain.entity.dictionary;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.mybatis.plus.baisc.VersionEntity;
import com.github.dactiv.service.resource.domain.meta.DataDictionaryMeta;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;

/**
 * <p>数据字典实体类</p>
 * <p>Table: tb_data_dictionary - 数据字典</p>
 *
 * @author maurice
 * @since 2021-05-06 11:59:41
 */
@Data
@Alias("dataDictionary")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_data_dictionary", autoResultMap = true)
public class DataDictionaryEntity extends DataDictionaryMeta implements VersionEntity<Integer, Integer> {

    @Serial
    private static final long serialVersionUID = 4219144269288469584L;

    public static final String TYPE_ID_COLUMN_NAME = "type_id";

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

    /**
     * 版本号
     */
    @Version
    private Integer version;

    /**
     * 是否启用:0.禁用,1.启用
     */
    @NotNull
    private DisabledOrEnabled enabled;

    /**
     * 对应字典类型
     */
    private Integer typeId;

    /**
     * 顺序值
     */
    private Integer sort = Integer.MAX_VALUE / 1000000;

    /**
     * 备注
     */
    private String remark;
}

