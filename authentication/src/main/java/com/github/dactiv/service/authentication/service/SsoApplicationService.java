package com.github.dactiv.service.authentication.service;

import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.service.authentication.dao.SsoApplicationDao;
import com.github.dactiv.service.authentication.domain.entity.SsoApplicationEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * tb_sso_application 的业务逻辑
 *
 * <p>Table: tb_sso_application - 单点登陆应用</p>
 *
 * @author maurice.chen
 * @see SsoApplicationEntity
 * @since 2023-11-29 10:22:33
 */
@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class SsoApplicationService extends BasicService<SsoApplicationDao, SsoApplicationEntity> {

}
