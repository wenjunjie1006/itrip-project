package com.cskt.itripauth.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.cskt.itripauth.common.constants.ErrorCodeEnum;
import com.cskt.itripauth.common.exception.ServiceException;
import com.cskt.itripauth.service.SmsService;
import com.cskt.util.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Override
    public void sendMsg(String to, String code) {
        //这里我们直接写死用户的accessKeyId和secret
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou",
                "LTAI4GKwe2Wzv8heQ4EuwWrq", "tk4dxul5p30m1GuYcVFN6sFBlvwZJd");
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        //指定发送的号码，也就是用户注册的号码
        request.putQueryParameter("PhoneNumbers", to);
        //指定短信中的前缀
        request.putQueryParameter("SignName", "爱旅行");
        request.putQueryParameter("TemplateCode", "SMS_205580498");
        //指定变量，也就是生成的code
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("code", code);
        try {
            request.putQueryParameter("TemplateParam", JSONUtil.objectToJsonString(jsonMap));
            CommonResponse response = client.getCommonResponse(request);
            log.info("调用短信发送接口响应数据：{}",response.getData());
        } catch (ServerException e) {
            log.error(e.getMessage(), e);
            //封装成统一的异常并抛出
            throw new
                    ServiceException(ErrorCodeEnum.ERROR_CALLING_THIRD_PARTY_SERVICE);
        } catch (ClientException e) {
            log.error(e.getMessage(), e);
            //封装成统一的异常并抛出
            throw new
                    ServiceException(ErrorCodeEnum.ERROR_CALLING_THIRD_PARTY_SERVICE);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            //封装成统一的异常并抛出
            throw new
                    ServiceException(ErrorCodeEnum.ERROR_CALLING_THIRD_PARTY_SERVICE);
        }
    }
}

