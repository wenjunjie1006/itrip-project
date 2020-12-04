package com.cskt.itripauth.shiro;

import com.cskt.entity.User;
import com.cskt.itripauth.common.constants.ErrorCodeEnum;
import com.cskt.itripauth.common.exception.ServiceException;
import com.cskt.itripauth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 自定义认证及鉴权处理器
 */
@Slf4j
public class MyRealm extends AuthorizingRealm {

    @Resource
    private UserService userService;

    /**
     * 判断当前传递过来的token类型是否为自定义和JWTToken
     * @param token
     * @return
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    /**
     * 此期没有涉及到用户的角色，所只不需要实现鉴权
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    /**
     * 默认使用此方法进行用户正确与否验证，错误抛出异常即可
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //获取token中的信息
        String jwtToken = (String) token.getCredentials();
        if (StringUtils.isEmpty(jwtToken)) {
            //如果当前token为空，表示没有传递过来
            throw new ServiceException(ErrorCodeEnum.AUTH_TOKEN_IS_EMPTY);
        }
        //获取jwt的负载信息，username,用于和数据库进行比对
        String username= JWTUtil.getUsername(jwtToken);
        if (StringUtils.isEmpty(username)){
            //如果为为空 ，表示用户不存在
            throw new ServiceException(ErrorCodeEnum.AUTH_UNKNOWN);
        }
        //根据传递过来的username，也就是userCode查询用户信息
        User user = userService.findUserByUserCode(username);
        if (user==null) {
            throw  new ServiceException(ErrorCodeEnum.AUTH_AUTHENTICATION_FAILED);
        }
        if (!JWTUtil.verify(jwtToken,username,user.getUserPassword())) {
            //当用户名和密码验证不通过
            throw  new ServiceException(ErrorCodeEnum.AUTH_AUTHENTICATION_FAILED);
        }
        return new SimpleAuthenticationInfo(jwtToken,jwtToken,"my_realm");
    }
}
