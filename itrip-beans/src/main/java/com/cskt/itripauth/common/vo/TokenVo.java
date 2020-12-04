package com.cskt.itripauth.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "登录token响应实体类")
public class TokenVo implements Serializable {
    private static final long serialVersionUID = 1443799713341690333L;
    @ApiModelProperty(value = "用户认证凭证")
    private String token;
    @ApiModelProperty(value = "过期时间")
    private Long expTime;
    @ApiModelProperty(value = "生成时间")
    private Long genTime;
}
