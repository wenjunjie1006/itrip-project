package com.cskt.itripauth.controller;

import com.cskt.entity.User;
import com.cskt.itripauth.common.constants.ErrorCodeEnum;
import com.cskt.itripauth.common.vo.ReturnResult;
import com.cskt.itripauth.common.vo.TokenVo;
import com.cskt.itripauth.service.UserService;
import com.cskt.itripauth.shiro.JWTUtil;
import com.cskt.util.JSONUtil;
import com.cskt.util.MD5;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * 登录控制器
 */
@Api(tags = "登录控制器")
@RestController
@RequestMapping(value = "/api")
@Slf4j
public class LoginController {
    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @ApiOperation(value = "用户登录接口", response = ReturnResult.class,
            httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE, protocols = "HTTP")
    @RequestMapping(value = "/dologin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    private ReturnResult login(
            @ApiParam(name = "name", value = "用户名")
            @RequestParam("name") String userCode,
            @ApiParam(name = "password", value = "用户登录密码")
            @RequestParam("password") String password) throws UnsupportedEncodingException {

        //先检验参数的准确性
        if (StringUtils.isEmpty(userCode) || StringUtils.isEmpty(password)) {
            return ReturnResult.error(ErrorCodeEnum.AUTH_TOKEN_IS_EMPTY);
        }
        User user = userService.findUserByUserCode(userCode);
        if (user == null) {
            //不建议怎么返回，在登录时如果出现用户名或密码错误一定要做模糊提示
            return ReturnResult.error(ErrorCodeEnum.AUTH_UNKNOWN);
        }
        if (!MD5.getMd5(password, 32).equals(user.getUserPassword())) {
            //密码验证不通过
            return ReturnResult.error(ErrorCodeEnum.AUTH_AUTHENTICATION_FAILED);

        }
        //生成JWTToken
        String jwtToken = JWTUtil.sign(userCode,MD5.getMd5(password,32));
        //封装成TokenVo
        TokenVo tokenVo = new TokenVo(jwtToken, System.currentTimeMillis() + 2 * 60 * 60 * 1000, System.currentTimeMillis());
        try {
            stringRedisTemplate.opsForValue().set(jwtToken, JSONUtil.objectToJsonString(user), 2, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            //序列化是出现异常
            return ReturnResult.error(ErrorCodeEnum.SYSTEM_EXECUTION_ERROR);

        }
        return ReturnResult.ok(tokenVo);
    }
}
