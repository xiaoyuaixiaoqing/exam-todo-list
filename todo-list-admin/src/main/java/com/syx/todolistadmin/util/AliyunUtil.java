package com.syx.todolistadmin.util;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class AliyunUtil {

    @Value("${aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String smsSignName;

    @Value("${aliyun.sms.template-code}")
    private String smsTemplateCode;

    @Value("${aliyun.email.from-address}")
    private String emailFromAddress;


    private final JavaMailSender mailSender;

    public AliyunUtil(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送短信
     */
    public boolean sendSms(String phone, String content) {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint("dysmsapi.aliyuncs.com");

            Client client = new Client(config);
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(smsSignName)
                    .setTemplateCode(smsTemplateCode)
                    .setTemplateParam("{\"content\":\"" + content + "\"}");

            SendSmsResponse response = client.sendSms(request);
            log.info("短信发送结果: {}", response.getBody().getMessage());
            return "OK".equals(response.getBody().getCode());
        } catch (Exception e) {
            log.error("短信发送失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送邮件
     */
    public boolean sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("邮件发送成功: {}", to);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            return false;
        }
    }
}
