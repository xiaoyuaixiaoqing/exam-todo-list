package com.syx.todolistadmin.service;

import com.syx.todolistadmin.entity.TaskLog;

public interface TaskLogService {
    void log(Long taskId, Long userId, String action, String oldValue, String newValue);
}
