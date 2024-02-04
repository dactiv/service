package com.github.dactiv.service.message.domain.meta.sms;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.service.commons.service.domain.meta.IdValueMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * 短信余额实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class SmsBalanceMeta extends IdEntity<String> {

    @Serial
    private static final long serialVersionUID = 4834851659384448629L;

    /**
     * 据道名称
     */
    private String channel;

    /**
     * 余额
     */
    private List<RestResult<IdValueMeta<String, BigDecimal>>> balances;
}
