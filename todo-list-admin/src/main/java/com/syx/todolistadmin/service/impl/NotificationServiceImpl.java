package com.syx.todolistadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syx.todolistadmin.entity.Notification;
import com.syx.todolistadmin.entity.User;
import com.syx.todolistadmin.mapper.NotificationMapper;
import com.syx.todolistadmin.mapper.UserMapper;
import com.syx.todolistadmin.service.NotificationService;
import com.syx.todolistadmin.util.AliyunUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {


    private final UserMapper userMapper;

    private final AliyunUtil aliyunUtil;

    @Override
    public void createNotification(Long userId, Long taskId, String type, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTaskId(taskId);
        notification.setType(type);
        notification.setContent(content);
        notification.setStatus(Integer.valueOf("unread"));
        notification.setSendTime(LocalDateTime.now());
        save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return list(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getSendTime));
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getStatus, "unread"));
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification != null) {
            notification.setStatus(Integer.valueOf("read"));
            updateById(notification);
        }
    }

    @Override
    public void deleteNotification(Long notificationId) {
        removeById(notificationId);
    }

    @Override
    @Async
    public void sendNotification(Notification notification) {
        User user = userMapper.selectById(notification.getUserId());
        if (user == null) {
            log.error("用户不存在: {}", notification.getUserId());
            return;
        }

        // 发送短信
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            boolean smsSuccess = aliyunUtil.sendSms(user.getPhone(), notification.getContent());
            log.info("短信发送{}: {}", smsSuccess ? "成功" : "失败", user.getPhone());
        }

        // 发送邮件
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            boolean emailSuccess = aliyunUtil.sendEmail(
                    user.getEmail(),
                    "任务提醒",
                    notification.getContent()
            );
            log.info("邮件发送{}: {}", emailSuccess ? "成功" : "失败", user.getEmail());
        }

        // 更新通知状态
        notification.setStatus(Integer.valueOf("sent"));
        updateById(notification);
    }
}
