package com.syx.todolistadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syx.todolistadmin.entity.Task;
import com.syx.todolistadmin.mapper.TaskMapper;
import com.syx.todolistadmin.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskMapper taskMapper;

    @Override
    public Task create(Task task) {
        taskMapper.insert(task);
        return task;
    }

    @Override
    public Task update(Task task) {
        taskMapper.updateById(task);
        return task;
    }

    @Override
    public void delete(Long id) {
        taskMapper.deleteById(id);
    }

    @Override
    public Task getById(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public IPage<Task> list(Long userId, Integer page, Integer size) {
        return taskMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Task>().eq(Task::getUserId, userId).orderByDesc(Task::getCreateTime));
    }
}
