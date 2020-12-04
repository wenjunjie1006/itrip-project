package com.cskt.itripauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cskt.itripauth.common.constants.ErrorCodeEnum;
import com.cskt.itripauth.common.condition.UserRegisterCondition;
import com.cskt.itripauth.common.constants.SystemConstants;
import com.cskt.itripauth.common.exception.ServiceException;
import com.cskt.itripauth.service.MailService;
import com.cskt.itripauth.service.SmsService;
import com.cskt.util.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cskt.entity.User;
import com.cskt.mapper.UserMapper;
import com.cskt.itripauth.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /*用户注册验证码的前缀*/
    private static final String ACTIVE_CODE_PRE = "active";

    /*是否发送邮箱开关*/
    @Value(value = "${email.send.enable}")
    private boolean enableSendEmail;

    /*是否发送短信的开关*/
    @Value(value = "${sms.send.enable}")
    private boolean enableSendSms;

    @Resource
    private MailService mailService;

    @Resource
    private SmsService smsService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean userRegister(UserRegisterCondition condition,String registerType) {
            LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>().lambda()
                    .eq(User::getUserCode, condition.getUserCode());
            User user = this.getOne(queryWrapper);
            if (user != null) {
                //记录日志
                log.warn("该用户已存在！");
                //用户已经存在，直接抛出异常
                throw new ServiceException(ErrorCodeEnum.AUTH_USER_ALREADY_EXISTS);
            }
            //处理数据并完成类型转换
            condition.setUserPassword(MD5.getMd5(condition.getUserPassword(), 32));
            User registerUser = new User();
            /* BeanUtils提供对Java反射和自省API的包装。其主要目的是利用反射机制对JavaBean的属性进行处理。*/
            BeanUtils.copyProperties(condition, registerUser);
            //指定用户类型，默认为系统自注册用户
            registerUser.setUserType(SystemConstants.UserType.REGISTRATION);
            //执行用户新增
            this.save(registerUser);
            //开始根据不同的注册类型,执行不同的验证码生成策略及发送信息或邮件
            switch (registerType){
                case "mail":
                    //生成激活码
                    String activeCode = MD5.getMd5(String.valueOf(System.currentTimeMillis()), 32);
                    log.info("激活码：{}", activeCode);
                    //存入redis，并设置过期时长为30分钟
                    stringRedisTemplate.opsForValue().set(ACTIVE_CODE_PRE + condition.getUserCode(), activeCode, 30, TimeUnit.MINUTES);
                    //发送激活码邮件
                    if (enableSendEmail) {
                        mailService.sendActivationMail(registerUser.getUserCode(), activeCode);
                    }
                    break;
                case "phone":
                    //生成手机验证码
                    int randomCode = MD5.getRandomCode();
                    log.info("验证码:{}",randomCode);
                    //将手机验证码存入redis,并使之过期时间为5分钟
                    stringRedisTemplate.opsForValue().
                            set(ACTIVE_CODE_PRE + condition.getUserCode(),
                            String.valueOf(randomCode),
                            5, TimeUnit.MINUTES);
                    if (enableSendSms){
                        smsService.sendMsg(condition.getUserCode(),String.valueOf(randomCode));
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    @Override
    public boolean active(@NotNull String userCode,@NotNull String code) {
        if (stringRedisTemplate.hasKey(ACTIVE_CODE_PRE + userCode)) {
            //先验证激活码是否有效
            String activeCode = stringRedisTemplate.opsForValue().get(ACTIVE_CODE_PRE + userCode);
            if (StringUtils.hasText(activeCode)) {
                if (code.equals(activeCode)) {
                    LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>().lambda()
                            .eq(User::getUserCode, userCode);
                    User user = this.getOne(queryWrapper);
                    if (user != null) {
                        //2、更新用户激活状态
                        user.setActivated(SystemConstants.UserActiveStatus.IS_ACTIVE);
                        user.setFlatId(user.getId());
                        user.setUserType(SystemConstants.UserType.REGISTRATION);
                        this.updateById(user);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean validatePhone(String phoneNum, String code) {
        //对比验证码
        String key = ACTIVE_CODE_PRE +phoneNum;
        //从redis中取出验证码
        String value = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在且是否相同
        if (StringUtils.hasText(value)&&value.equals(code)) {
            //验证通过
            LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                    .lambda().eq(User::getUserCode, phoneNum);
            User user = this.getOne(queryWrapper);
            if (user != null) {
                //更新用户激活状态
                user.setActivated(SystemConstants.UserActiveStatus.IS_ACTIVE);
                //平台id修改为当前账户
                user.setFlatId(user.getId());
                user.setUserType(SystemConstants.UserType.REGISTRATION);
                this.updateById(user);
                return true;
            }
        }
        return false;
    }

    @Override
    public User findUserByUserCode(String userCode) {
        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .lambda().eq(User::getUserCode, userCode);
        return  this.getOne(queryWrapper);
    }
}
