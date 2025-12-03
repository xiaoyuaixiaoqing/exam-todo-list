package com.syx.todolistadmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 邮件配置类
 * 用于在缺少邮件配置时提供一个默认的JavaMailSender实现
 */
@Configuration
public class MailConfig {

    @Bean
    @Profile("!prod") // 非生产环境提供默认实现
    public JavaMailSender javaMailSender() {
        // 创建一个简单的JavaMailSender实现
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        return mailSender;
    }
}