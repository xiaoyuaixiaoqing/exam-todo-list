package com.syx.todolistadmin.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.syx.todolistadmin.entity.Task;
import com.syx.todolistadmin.entity.TaskRecycle;
import com.syx.todolistadmin.mapper.TaskMapper;
import com.syx.todolistadmin.mapper.TaskRecycleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskCleanupScheduler {
    private final TaskRecycleMapper taskRecycleMapper;
    private final TaskMapper taskMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredRecycleBin() {
        List<TaskRecycle> expired = taskRecycleMapper.selectList(
                new LambdaQueryWrapper<TaskRecycle>().lt(TaskRecycle::getExpireTime, LocalDateTime.now()));
        expired.forEach(r -> {
            taskMapper.deleteById(r.getTaskId());
            taskRecycleMapper.deleteById(r.getId());
        });
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void updateOverdueTasks() {
        taskMapper.update(null, new LambdaUpdateWrapper<Task>()
                .set(Task::getStatus, 4)
                .lt(Task::getDueDate, LocalDateTime.now())
                .in(Task::getStatus, 0, 1)
                .eq(Task::getIsDeleted, 1));
    }
}
