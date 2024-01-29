package com.github.dactiv.service.resource.domain.entity.dictionary;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>字典类型实体</p>
 * <p>Table: tb_dictionary_type - 字典类型</p>
 *
 * @author maurice
 * @since 2021-05-06 11:59:41
 */
@Data
@Alias("dictionaryType")
@TableName("tb_dictionary_type")
@EqualsAndHashCode(callSuper = true)
public class DictionaryTypeEntity extends IntegerVersionEntity<Integer> implements Tree<Integer, DictionaryTypeEntity> {

    @Serial
    private static final long serialVersionUID = 2211302874891670273L;

    /**
     * 键名称
     */
    @NotEmpty
    @Length(max = 128)
    private String code;

    /**
     * 类型名称
     */
    @NotEmpty
    @Length(max = 64)
    private String name;

    /**
     * 父字典类型,根节点为 null
     */
    private Integer parentId;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private List<Tree<Integer, DictionaryTypeEntity>> children = new LinkedList<>();

    @Override
    @JsonIgnore
    public Integer getParent() {
        return parentId;
    }

}


