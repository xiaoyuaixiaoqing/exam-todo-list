package com.syx.todolistadmin.service.impl;

import com.syx.todolistadmin.entity.TaskLog;
import com.syx.todolistadmin.mapper.TaskLogMapper;
import com.syx.todolistadmin.service.TaskLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskLogServiceImpl implements TaskLogService {
    private final TaskLogMapper taskLogMapper;

    @Override
    public void log(Long taskId, Long userId, String action, String oldValue, String newValue) {
        TaskLog log = new TaskLog();
        log.setTaskId(taskId);
        log.setUserId(userId);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        taskLogMapper.insert(log);
    }
}
