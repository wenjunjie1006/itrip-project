package com.cskt.itripauth.common.condition;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "UserRegisterCondition", description = "用户注册条件实体类")
public class UserRegisterCondition implements Serializable {

    private static final long serialVersionUID = -1183295154379120441L;
    @ApiModelProperty(value = "用户账号")
    private String userCode;
    @ApiModelProperty(value = "用户名")
    private String userName;
    @ApiModelProperty(value = "用户密码")
    private String userPassword;
}
