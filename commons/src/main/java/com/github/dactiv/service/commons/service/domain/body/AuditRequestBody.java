package com.github.dactiv.service.commons.service.domain.body;

import com.github.dactiv.service.commons.service.enumerate.AuditStatusEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 审核请求体
 *
 * @author maurice.chen
 */

@Data
public class AuditRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1497402881361194405L;

    private Integer id;

    private AuditStatusEnum status;

    private String auditRemark;

}
