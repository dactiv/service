package com.github.dactiv.service.commons.service.feign;

import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.service.commons.service.SystemConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 认证服务 feign 客户端
 *
 * @author maurice
 */
@FeignClient(value = SystemConstants.SYS_AUTHENTICATION_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface AuthenticationServiceFeignClient {

    /**
     * 获取最后一次认证信息
     *
     * @param userDetails 用户明细
     * @return 认证信息
     */
    @PostMapping("info/getLastByUserDetails")
    Map<String, Object> getLastAuthenticationInfo(@RequestBody TypeUserDetails<Integer> userDetails);

    /**
     * 获取系统用户
     *
     * @param userDetails 用户明细
     * @return 系统用户
     */
    @PostMapping("authentication/getSystemUser")
    Map<String, Object> getSystemUser(@RequestBody TypeUserDetails<Integer> userDetails);


}
