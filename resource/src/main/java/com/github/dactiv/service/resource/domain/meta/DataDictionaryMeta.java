package com.github.dactiv.service.resource.domain.meta;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.service.commons.service.domain.meta.ResourceDictionaryMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 数据字典元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataDictionaryMeta extends ResourceDictionaryMeta implements BasicIdentification<Integer>, Tree<Integer, DataDictionaryMeta> {

    @Serial
    private static final long serialVersionUID = -6880817354929730676L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    /**
     * 根节点为 null
     */
    private Integer parentId;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> meta = new LinkedHashMap<>();

    /**
     * 子类节点
     */
    @TableField(exist = false)
    private List<Tree<Integer, DataDictionaryMeta>> children = new LinkedList<>();

    @JsonIgnore
    @Override
    public Integer getParent() {
        return parentId;
    }
}
