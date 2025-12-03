package com.syx.todolistadmin.controller;

import com.syx.todolistadmin.common.Result;
import com.syx.todolistadmin.entity.Notification;
import com.syx.todolistadmin.service.NotificationService;
import com.syx.todolistadmin.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;


    /**
     * 获取当前用户通知列表
     */
    @GetMapping
    public Result<List<Notification>> getNotifications(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return Result.success(notifications);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success("标记成功");
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success("删除成功");
    }
}
