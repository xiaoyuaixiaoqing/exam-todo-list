package com.syx.todolistadmin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.syx.todolistadmin.entity.Task;

public interface TaskService {
    Task create(Task task);
    Task update(Task task);
    void delete(Long id);
    Task getById(Long id);
    IPage<Task> list(Long userId, Integer page, Integer size);
}
