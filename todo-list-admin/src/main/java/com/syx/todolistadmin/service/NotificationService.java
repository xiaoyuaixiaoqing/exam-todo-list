package com.syx.todolistadmin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syx.todolistadmin.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    /**
     * 创建通知
     */
    void createNotification(Long userId, Long taskId, String type, String content);

    /**
     * 获取用户通知列表
     */
    List<Notification> getUserNotifications(Long userId);

    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId);

    /**
     * 发送通知（短信/邮件）
     */
    void sendNotification(Notification notification);
}
