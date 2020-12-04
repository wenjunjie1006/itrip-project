package com.cskt.itripauth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cskt.entity.User;
import com.cskt.itripauth.common.condition.UserRegisterCondition;
import com.cskt.itripauth.common.constants.ErrorCodeEnum;
import com.cskt.itripauth.common.vo.ReturnResult;
import com.cskt.itripauth.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.regex.Pattern;

@RestController
@Api(tags = "用户相关控制器")
@RequestMapping(value = "/api")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 校验用户是否已存在
     *
     * @param name
     * @return
     */
    /*@ApiOperation(value = “接口说明”, httpMethod = “接口请求方式”, response = “接口返回参数类型”, notes = “接口发布说明”*/
    /*@ApiParam(required = “是否必须参数”, name = “参数名称”, value = “参数具体描述”*/
    @ApiOperation(value = "检查用户名是否已存在", httpMethod = "GET",
            protocols = "HTTP", produces = MediaType.APPLICATION_JSON_VALUE,
            response = ReturnResult.class, notes = "验证是否已存在该用户名")
    @RequestMapping(value = "/ckuser", method = RequestMethod.GET)
    public ReturnResult checkUser(
            @ApiParam(name = "name", value = "待验证用户名",
                    defaultValue = "text@bdqn.cn") @RequestParam String name) {
        if (StringUtils.isEmpty(name)) {
            //参数是否为空
            return ReturnResult.error(ErrorCodeEnum.AUTH_PARAMETER_IS_EMPTY);
        }
        //判断用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>().lambda().eq(User::getUserCode, name);
        User user = userService.getOne(queryWrapper);
        if (user != null) {
            //当用户数据不为空的时候，校验不通过
            return ReturnResult.error(ErrorCodeEnum.AUTH_USER_ALREADY_EXISTS);
        }
        return ReturnResult.ok();
    }

    /**
     * 邮箱注册
     *
     * @param condition
     * @return
     */
    @ApiOperation(value = "邮箱注册")
    @RequestMapping(value = "/doregister", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResult doRegister(@RequestBody UserRegisterCondition condition) {
        //校验邮箱地址是否符合要求
        if (!validEmail(condition.getUserCode())) {
            return ReturnResult.error(ErrorCodeEnum.AUTH_ILLEGAL_USERCODE);
        }

        boolean result = userService.userRegister(condition, "mail");
        if (result) {
            return ReturnResult.ok();
        }
        return ReturnResult.error();
    }

    /**
     * 通过正则表达式校验邮箱地址是否符合要求
     * 合法E-mail地址：
     * 1. 必须包含一个并且只有一个符号“@”
     * 2. 第一个字符不得是“@”或者“.”
     * 3. 不允许出现“@.”或者.@
     * 4. 结尾不得是字符“@”或者“.”
     * 5. 允许“@”前的字符中出现“＋”
     * 6. 不允许“＋”在最前面，或者“＋@”
     *
     * @param email 邮箱验证地址
     * @return
     */
    private boolean validEmail(String email) {
        String regex = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        return Pattern.compile(regex).matcher(email).find();
    }

    /**
     * 邮箱验证
     *
     * @param user
     * @param code
     * @return
     */
    @ApiOperation(value = "邮箱激活")
    @RequestMapping(value = "/activateByEmail", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResult activateByEmail(
            @ApiParam(name = "user",
                    value = "注册邮箱地址",
                    defaultValue = "2331073264@qq.com")
            @RequestParam String user,
            @ApiParam(name = "code",
                    value = "激活码",
                    defaultValue = "fba66b712d3299053e754ed785ad1ef1")
            @RequestParam String code) {
        //判断参数是否为空
        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(code)) {
            return ReturnResult.error(ErrorCodeEnum.AUTH_PARAMETER_IS_EMPTY);
        }

        boolean activeResult = userService.active(user, code);
        if (activeResult) {
            return ReturnResult.ok();
        } else {
            return ReturnResult.error();
        }
    }

    /**
     * 手机号码注册
     *
     * @param condition
     * @return
     */
    @ApiOperation(value = "手机号码注册")
    @RequestMapping(value = "doRegisterByPhone", method = RequestMethod.POST)
    public ReturnResult doRegisterByPhone(@RequestBody UserRegisterCondition condition) {
        //手机号格式验证
        if (!this.validPhone(condition.getUserCode())) {
            return ReturnResult.error(ErrorCodeEnum.AUTH_ILLEGAL_USERCODE);
        }
        boolean result = userService.userRegister(condition, "phone");
        if (result) {
            return ReturnResult.ok();
        }
        return ReturnResult.error();
    }

    /**
     * 验证手机号码的格式是否正确
     *
     * @param phone 手机号码
     * @return 返回true表示手机号码验证通过，否则返回false
     */
    public boolean validPhone(String phone) {
        String regex = "0?(13|14|15|17|18|19)[0-9]{9}";
        return Pattern.compile(regex).matcher(phone).find();
    }

    /**
     * 手机号码激活
     *
     * @param user
     * @param code
     * @return
     */
    @ApiOperation(value = "手机号码验证")
    @RequestMapping(value = "activateByPhone", method = RequestMethod.PUT)
    public ReturnResult activateByPhone(
            @ApiParam(name = "user", value = "手机号码")
            @RequestParam String user,
            @ApiParam(name = "code", value = "验证码")
            @RequestParam String code) {
        try {
            if (userService.validatePhone(user, code)) {
                return ReturnResult.ok();
            } else {
                return ReturnResult.error();
            }
        } catch (Exception e) {
            return ReturnResult.error(ErrorCodeEnum.AUTH_ACTIVATE_FAILED);
        }
    }
}
