package com.cskt.itripauth.service;

public interface SmsService {
    /**
     * 用于发送短信
     * @param to 短信发送给谁
     * @param code 验证码
     */
    void sendMsg(String to, String code);
}
