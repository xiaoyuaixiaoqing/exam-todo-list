package com.syx.todolistadmin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.syx.todolistadmin.common.Result;
import com.syx.todolistadmin.entity.TaskLog;
import com.syx.todolistadmin.mapper.TaskLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    private final TaskLogMapper taskLogMapper;

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
}
