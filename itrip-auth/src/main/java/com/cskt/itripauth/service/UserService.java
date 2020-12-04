package com.cskt.itripauth.service;

import com.cskt.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cskt.itripauth.common.condition.UserRegisterCondition;

public interface UserService extends IService<User>{
    /**
     * 邮箱用户注册
     * @param condition 前端传递过来的用户注册参数
     * @param registerType 注册类型
     * @return 注册结果 true:成功 ; false:失败
     */
    boolean userRegister(UserRegisterCondition condition,String registerType);

    /**
     * 邮箱激活用户
     *
     * @param userCode
     * @param code
     * @return
     */
    boolean active(String userCode, String code);

    /**
     * 短信验证
     * @param phoneNum  手机号码
     * @param code      验证码
     * @return  true表示验证成功，false表示验证失败
     */
    boolean validatePhone(String phoneNum,String code);

    /**
     * 根据用户名查询用户信息，主要是封装查询条件，对外提供统一的方法
     * @param userCode
     * @return
     */
    User findUserByUserCode(String userCode);

}
