package com.github.dactiv.service.message.domain.body.site;

import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.service.message.domain.entity.SiteMessageEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReadSiteMessageResponseBody extends SiteMessageEntity {

    @Serial
    private static final long serialVersionUID = 1958479800640562304L;

    private YesOrNo beforeReadable;
}
