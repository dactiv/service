package com.github.dactiv.service.resource.domain.entity.access;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Date;

/**
 * <p>访问加解密断言扩展实体类</p>
 * <p>Table: tb_access_crypto_predicate - 访问加解密断言</p>
 *
 * @author maurice
 * @since 2021-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Alias("configAccessCryptoPredicate")
@TableName("tb_access_crypto_predicate")
public class AccessCryptoPredicateEntity extends AccessCryptoPredicate implements NumberIdEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 5764360440250914629L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 访问加解密 id
     */
    private Integer accessCryptoId;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Date creationTime = new Date();

}
