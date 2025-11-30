package com.syx.todolistadmin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.syx.todolistadmin.entity.Task;

import java.util.List;

public interface TaskService {
    Task create(Task task);
    Task update(Task task);
    void delete(Long id);
    Task getById(Long id);
    IPage<Task> list(Long userId, Integer page, Integer size, String category, Integer status, Integer priority, String sortBy);
    List<Task> search(Long userId, String keyword);
    List<Task> getRecycleBin(Long userId);
    void restore(Long id);
    void permanentDelete(Long id);
    List<Task> getOverdueTasks(Long userId);
}
