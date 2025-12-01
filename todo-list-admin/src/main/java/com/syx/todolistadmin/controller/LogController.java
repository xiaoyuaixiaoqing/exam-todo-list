package com.syx.todolistadmin.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.syx.todolistadmin.common.Result;
import com.syx.todolistadmin.entity.Task;
import com.syx.todolistadmin.entity.TaskLog;
import com.syx.todolistadmin.mapper.TaskLogMapper;
import com.syx.todolistadmin.mapper.TaskMapper;
import com.syx.todolistadmin.service.TaskLogService;
import com.syx.todolistadmin.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    private final TaskLogMapper taskLogMapper;
    private final TaskMapper taskMapper;
    private final TaskLogService taskLogService;
    private final TaskService taskService;

    @GetMapping("/tasks/{taskId}")
    public Result<List<TaskLog>> getTaskLogs(@PathVariable Long taskId) {
        return Result.success(taskLogMapper.selectList(
                new LambdaQueryWrapper<TaskLog>()
                        .eq(TaskLog::getTaskId, taskId)
                        .orderByDesc(TaskLog::getCreateTime)));
    }

    @GetMapping("/users/{userId}")
    public Result<List<TaskLog>> getUserLogs(@PathVariable Long userId) {
        return Result.success(taskLogMapper.selectList(
                new LambdaQueryWrapper<TaskLog>()
                        .eq(TaskLog::getUserId, userId)
                        .orderByDesc(TaskLog::getCreateTime)));
    }

    @PostMapping("/tasks/{taskId}/rollback/{logId}")
    public Result<Void> rollbackToLog(@PathVariable Long taskId, @PathVariable Long logId, @RequestParam Long userId) {
        TaskLog log = taskLogMapper.selectById(logId);
        if (log == null || !log.getTaskId().equals(taskId)) {
            throw new RuntimeException("日志不存在");
        }

        String dataJson = log.getOldValue();
        if (dataJson == null) {
            throw new RuntimeException("无法回滚到此状态");
        }

        Task historyTask = JSON.parseObject(dataJson, Task.class);
        Task currentTask = taskMapper.selectById(taskId);
        if (currentTask != null) {
            String oldData = JSON.toJSONString(currentTask);
            historyTask.setVersion(currentTask.getVersion());
            historyTask.setUserId(userId);
            taskService.update(historyTask);
            taskLogService.log(taskId, userId, "ROLLBACK", oldData, JSON.toJSONString(historyTask));
        }

        return Result.success(null);
    }
}
