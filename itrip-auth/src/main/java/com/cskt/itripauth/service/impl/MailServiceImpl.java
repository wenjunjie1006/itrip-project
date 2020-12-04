package com.cskt.itripauth.service.impl;

import com.cskt.itripauth.common.constants.ErrorCodeEnum;
import com.cskt.itripauth.common.exception.ServiceException;
import com.cskt.itripauth.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class MailServiceImpl implements MailService {

    @Resource
    private MailSender mailSender;

    @Override
    public void sendActivationMail(String mailTo, String activationCode) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            //指定需要发送的地址
            simpleMailMessage.setTo(mailTo);
            //指定需要发送的内容
            simpleMailMessage.setText("您的激活码是："+activationCode);
            //执行发送
            mailSender.send(simpleMailMessage);
        } catch (MailException e) {
            log.warn(e.getMessage(),e);
            throw new ServiceException(ErrorCodeEnum.ERROR_CALLING_THIRD_PARTY_SERVICE);
        }
    }
}
