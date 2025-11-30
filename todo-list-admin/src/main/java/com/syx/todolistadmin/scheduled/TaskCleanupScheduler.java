package com.syx.todolistadmin.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}
