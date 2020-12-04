package com.cskt.itripauth.shiro;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro配置类
 */
@Configuration
public class ShiroConfig {
    /**
     * 配置统一的安全管理器
     *
     * @param myRealm
     * @return
     */
    @Bean(name = "securityManager")
    public DefaultSecurityManager securityManager(@Qualifier("myRealm") MyRealm myRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //将安全管理器与自定义处理器完成绑定
        securityManager.setRealm(myRealm);
        return securityManager;
    }

    /**
     * 将自定义的认证处理器纳入Spring 管理
     *
     * @return
     */
    @Bean("myRealm")
    public MyRealm myRealm() {
        return new MyRealm();
    }

    /**
     * 配置自定义过滤器及路径过滤规则
     *
     * @param securityManager
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("securityManager") DefaultSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new
                ShiroFilterFactoryBean();
        // 添加自己的过滤器并且取名为jwt
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", new JWTFilter());
        shiroFilterFactoryBean.setFilters(filterMap);
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setUnauthorizedUrl("/401");
        /**
         *自定义url规则
         * http://shiro.apache.org/web.html#urls-
         */
        Map<String, String> filterRuleMap = new LinkedHashMap<String, String>();
        //登录请求不用通过自定义过滤器,anon表示不需要验证直接放行
        filterRuleMap.put("api/dologin", "anon");
        //注册也需要被放行  todd：2020/11/18

        //swagger-ui
        filterRuleMap.put("/swagger-ui.html", "anon");
        filterRuleMap.put("/swagger-ui.html/**", "anon");
        filterRuleMap.put("/swagger-resources/**", "anon");
        filterRuleMap.put("/webjars/**", "anon");
        filterRuleMap.put("/v2/**", "anon");

        //其他的所有请求,全部通过自定义过滤器
        filterRuleMap.put("/**","jwt");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterRuleMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 下面的代码是添加注解支持
     *
     * @return
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 将安全管理器整合到项目中
     *
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
}
