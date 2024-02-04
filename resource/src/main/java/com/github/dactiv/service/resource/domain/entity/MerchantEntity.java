package com.github.dactiv.service.resource.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;

/**
 * <p>Table: tb_merchant - 商户表</p>
 *
 * @author maurice.chen
 * @since 2023-09-11 08:57:11
 */
@Data
@NoArgsConstructor
@Alias("merchant")
@TableName("tb_merchant")
@EqualsAndHashCode(callSuper = true)
public class MerchantEntity extends IntegerVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 2170545239707350962L;

    /**
     * 名称
     */
    @NotNull
    private String name;

    /**
     * app id
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String appId;

    /**
     * app key
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String appKey;

    /**
     * 备注
     */
    private String remark;

}