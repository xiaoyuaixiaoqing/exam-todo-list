package com.syx.todolistadmin.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.syx.todolistadmin.entity.Notification;
import com.syx.todolistadmin.entity.Task;
import com.syx.todolistadmin.mapper.TaskMapper;
import com.syx.todolistadmin.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final TaskMapper taskMapper;

    private final NotificationService notificationService;

    /**
     * 每小时检查即将到期的任务，提前24小时通知
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkUpcomingTasks() {
        log.info("开始检查即将到期的任务...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);

        // 查询24小时内到期的未完成任务
        List<Task> upcomingTasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getStatus, "pending")
                .between(Task::getDueDate, now, tomorrow));

        for (Task task : upcomingTasks) {
            // 创建通知
            String content = String.format("任务【%s】将在24小时内到期，请及时处理！", task.getTitle());
            notificationService.createNotification(
                    task.getUserId(),
                    task.getId(),
                    "reminder",
                    content
            );

            // 发送短信/邮件
            Notification notification = new Notification();
            notification.setUserId(task.getUserId());
            notification.setTaskId(task.getId());
            notification.setType("reminder");
            notification.setContent(content);
            notificationService.sendNotification(notification);

            log.info("已发送任务提醒: taskId={}, userId={}", task.getId(), task.getUserId());
        }

        log.info("任务提醒检查完成，共发送{}条通知", upcomingTasks.size());
    }
}
