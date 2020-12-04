package com.cskt.itripauth.shiro;

import com.cskt.itripauth.common.constants.ErrorCodeEnum;
import com.cskt.itripauth.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt过滤器
 */
@Slf4j
public class JWTFilter extends BasicHttpAuthenticationFilter {
    /**
     * 判断用户是否已经登录
     * 检测cookie中的token对应的值是否为空
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        log.info("请求路径为：{}", req.getRequestURI());
        //取出cookie中的数据
        Cookie[] cookies = req.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if ("token".equals(cookie.getName())) {
                //取出cookie中token对应的值
                String token = cookie.getValue();
                log.info("token:{}", token);
                //如果当前token的值不为空则当前用户已登录
                return StringUtils.hasText(token);
            }
        }
        return false;
    }

    /**
     * 如果用户没有登录，则调用自定义的Realm执行登录
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response)
            throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        //取出cookie中的数据
        Cookie[] cookies = httpServletRequest.getCookies();
        String token = null;
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if ("token".equals(cookie.getName())) {
                //取出cookie中token对应的值
                token = cookie.getValue();
                break;
            }
        }
        log.info("token:{}", token);
        JWTToken jwtToken = new JWTToken(token);
        //提交给realm进行登入，如果错误它会抛出异常并被捕获
        getSubject(request, response).login(jwtToken);
        //如果没有抛出异常，则代表登录成功，返回true
        return true;
    }

    /**
     * 这里详细说明一下为什么最终返回的都是true，即允许访问
     * 例如我们提供一个地址 GET/article
     * 登录用户和游客看到的内容是不同的
     * 如果在这里返回了false，请求会被直接拦截，用户看不到任何东西
     * 所以我们在这里返回true，controller中可以通过subject.isAuthenticated()来判断用户是否登录
     * 如果有些资源只有登录用户才能访问，我们只需要在方法上加上 @RequireAuthentication注解即可
     * 但是这样做有一个缺点，就是不能够对GET，POST等请求进行分别过滤鉴权（因为我们重写了官方的方法），但实际上对应用影响不大
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (!isLoginAttempt(request, response)) {
            try {
                //当用户未登录的时候执行登录操作，如果出现异常，则可以考虑抛出异常
                executeLogin(request, response);
            } catch (Exception e) {
                //用户未登录
                throw new ServiceException(ErrorCodeEnum.AUTH_NOT_LOGIN);
            }
        }
        return true;
    }

    /**
     * 对跨域提供支持
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response)
            throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        //跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}
