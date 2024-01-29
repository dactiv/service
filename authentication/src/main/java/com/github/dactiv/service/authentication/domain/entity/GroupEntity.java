package com.github.dactiv.service.authentication.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.annotation.JsonCollectionGenericType;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.mybatis.handler.JacksonJsonTypeHandler;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.*;

/**
 * <p>用户组实体类</p>
 * <p>Table: tb_group - 用户组表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@Alias("group")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_group", autoResultMap = true)
public class GroupEntity extends IntegerVersionEntity<Integer> implements Tree<Integer, GroupEntity> {

    @Serial
    private static final long serialVersionUID = 5357157352791368716L;

    /**
     * 名称
     */
    @NotEmpty
    @Length(max = 32)
    private String name;

    /**
     * spring security role 的 authority 值
     */
    @NotEmpty
    @Length(max = 64)
    private String authority;

    /**
     * 来源
     *
     * @see Plugin#sources()
     */
    @NotNull
    @JsonCollectionGenericType(ResourceSourceEnum.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<ResourceSourceEnum> sources = new LinkedList<>();

    /**
     * 父类 id
     */
    private Integer parentId;

    /**
     * 是否可删除:0.否、1.是
     */
    @NotNull
    private YesOrNo removable;

    /**
     * 是否可修改:0.否、1.是
     */
    @NotNull
    private YesOrNo modifiable;

    /**
     * 状态:0.禁用、1.启用
     */
    @NotNull
    private DisabledOrEnabled status;

    /**
     * 资源 id 集合
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, List<String>> resourceMap = new LinkedHashMap<>();

    /**
     * 备注
     */
    private String remark;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Tree<Integer, GroupEntity>> children = new ArrayList<>();

    @Override
    @JsonIgnore
    public Integer getParent() {
        return parentId;
    }

}