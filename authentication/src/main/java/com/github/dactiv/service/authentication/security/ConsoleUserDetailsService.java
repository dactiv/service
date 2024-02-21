package com.github.dactiv.service.authentication.security;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.NameEnumUtils;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.security.enumerate.UserStatus;
import com.github.dactiv.framework.spring.security.authentication.AbstractUserDetailsService;
import com.github.dactiv.framework.spring.security.authentication.config.AccessTokenProperties;
import com.github.dactiv.framework.spring.security.authentication.config.SpringSecurityProperties;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RememberMeAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.framework.spring.security.authentication.token.SimpleAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.authentication.config.ApplicationConfig;
import com.github.dactiv.service.authentication.domain.entity.ConsoleUserEntity;
import com.github.dactiv.service.authentication.domain.entity.SystemUserEntity;
import com.github.dactiv.service.authentication.enumerate.AuthenticationTypeEnum;
import com.github.dactiv.service.authentication.service.ConsoleUserService;
import com.github.dactiv.service.commons.service.config.CommonsConfig;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;

/**
 * 系统用户明细认证授权服务实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class ConsoleUserDetailsService extends AbstractUserDetailsService {

    private final ConsoleUserService consoleUserService;

    private final CommonsConfig commonsConfig;

    private final PasswordEncoder passwordEncoder;

    private final AccessTokenProperties accessTokenProperties;

    @Override
    @Autowired
    public void setSpringSecurityProperties(SpringSecurityProperties springSecurityProperties) {
        super.setSpringSecurityProperties(springSecurityProperties);
    }

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token)
            throws AuthenticationException {

        String type = token.getHttpServletRequest().getParameter(ApplicationConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);
        if (StringUtils.isEmpty(type)) {
            throw new BadCredentialsException("参数 " + ApplicationConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME + " 不能为空");
        }

        AuthenticationTypeEnum loginType = NameEnumUtils.parse(type, AuthenticationTypeEnum.class);

        if (!AuthenticationTypeEnum.USERNAME.equals(loginType)) {
            throw new BadCredentialsException("该认证仅支持 " + AuthenticationTypeEnum.USERNAME + " 的 " + ApplicationConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME + " 方式进行认证");
        }

        ConsoleUserEntity user = consoleUserService.getByIdentity(token.getPrincipal().toString());

        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        if (UserStatus.Disabled.equals(user.getStatus())) {
            throw new DisabledException("您的账号已被禁用。");
        }

        return createUserDetails(user);
    }

    private SecurityUserDetails createUserDetails(ConsoleUserEntity user) {
        SecurityUserDetails userDetails = new SecurityUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus()
        );

        userDetails.setMeta(user.toSecurityUserDetailsMeta());
        userDetails.setProfile(user.toSecurityUserDetailsMetaProfile());
        userDetails.setType(ResourceSourceEnum.CONSOLE_SOURCE_VALUE);

        consoleUserService.getAuthorizationService().setSystemUserAuthorities(user, userDetails);

        return userDetails;
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(ResourceSourceEnum.CONSOLE_SOURCE_VALUE);
    }

    @Override
    public PrincipalAuthenticationToken createSuccessAuthentication(SecurityUserDetails userDetails,
                                                                    SimpleAuthenticationToken token,
                                                                    Collection<? extends GrantedAuthority> grantedAuthorities) {

        ConsoleUserEntity user = consoleUserService.get(Casts.cast(userDetails.getId(), Integer.class));
        user.setLastAuthenticationTime(new Date());

        consoleUserService.updateById(user);
        userDetails.getMeta().put(
                accessTokenProperties.getAccessTokenParamName(),
                consoleUserService
                        .getAuthorizationService()
                        .getAccessTokenContextRepository()
                        .generatePlaintextString(userDetails)
        );
        return super.createSuccessAuthentication(userDetails, token, grantedAuthorities);
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    public void updatePassword(SecurityUserDetails userDetails, String oldPassword, String newPassword) {
        Integer id = Casts.cast(userDetails.getId(), Integer.class);
        ConsoleUserEntity user = consoleUserService.get(id);
        Assert.isTrue(getPasswordEncoder().matches(oldPassword, user.getPassword()), "旧密码不正确");

        user.setPassword(passwordEncoder.encode(newPassword));
        consoleUserService.updateById(user);

        consoleUserService.getAuthorizationService().deleteSystemUserAllCache(user, ResourceSourceEnum.CONSOLE);
    }

    @Override
    public String adminRestPassword(Integer id) {

        String newPassword = RandomStringUtils.random(commonsConfig.getRestPasswordLength(), true, true);
        consoleUserService
                .lambdaUpdate()
                .set(SystemUserEntity::getPassword, getPasswordEncoder().encode(newPassword))
                .eq(IdEntity::getId, id)
                .update();

        ConsoleUserEntity user = consoleUserService.get(id);
        consoleUserService.getAuthorizationService().expireSystemUserSession(user, ResourceSourceEnum.CONSOLE);
        consoleUserService.getAuthorizationService().deleteSystemUserAllCache(user, ResourceSourceEnum.CONSOLE);

        return newPassword;
    }

    @Override
    public SecurityUserDetails getRememberMeUserDetails(RememberMeAuthenticationToken token) {
        ConsoleUserEntity user = consoleUserService.get(Casts.cast(token.getId(), Integer.class));
        return createUserDetails(user);
    }
}
