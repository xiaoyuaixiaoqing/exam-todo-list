package com.syx.todolistadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syx.todolistadmin.entity.Task;
import com.syx.todolistadmin.entity.TaskRecycle;
import com.syx.todolistadmin.mapper.TaskMapper;
import com.syx.todolistadmin.mapper.TaskRecycleMapper;
import com.syx.todolistadmin.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskMapper taskMapper;
    private final TaskRecycleMapper taskRecycleMapper;

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
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        TaskRecycle recycle = new TaskRecycle();
        recycle.setTaskId(id);
        recycle.setDeletedBy(task.getUserId());
        recycle.setExpireTime(LocalDateTime.now().plusDays(15));
        taskRecycleMapper.insert(recycle);
        taskMapper.deleteById(id);
    }

    @Override
    public Task getById(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public IPage<Task> list(Long userId, Integer page, Integer size, String category, Integer status, Integer priority, String sortBy) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>().eq(Task::getUserId, userId);
        if (category != null) wrapper.eq(Task::getCategory, category);
        if (status != null) {
            if (status == 4) {
                wrapper.ne(Task::getStatus, 2).lt(Task::getDueDate, LocalDateTime.now());
            } else {
                wrapper.eq(Task::getStatus, status);
            }
        }
        if (priority != null) wrapper.eq(Task::getPriority, priority);
        if ("priority".equals(sortBy)) wrapper.orderByDesc(Task::getPriority);
        else if ("dueDate".equals(sortBy)) wrapper.orderByAsc(Task::getDueDate);
        else wrapper.orderByDesc(Task::getCreateTime);
        return taskMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Task> search(Long userId, String keyword) {
        return taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .and(w -> w.like(Task::getTitle, keyword).or().like(Task::getDescription, keyword)));
    }

    @Override
    public List<Task> getRecycleBin(Long userId) {
        List<TaskRecycle> recycles = taskRecycleMapper.selectList(
                new LambdaQueryWrapper<TaskRecycle>().eq(TaskRecycle::getDeletedBy, userId));
        return recycles.stream()
                .map(r -> taskMapper.selectByIdIgnoreLogicDel(r.getTaskId()).stream().findFirst().orElse(null))
                .filter(t -> t != null && t.getIsDeleted() == 0)
                .toList();
    }

    @Override
    public void restore(Long id) {
        Task task = taskMapper.selectByIdIgnoreLogicDel(id).stream().filter(t -> t.getIsDeleted() == 0).findFirst().orElse(null);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        taskMapper.restoreTask(id);
        taskRecycleMapper.delete(new LambdaQueryWrapper<TaskRecycle>().eq(TaskRecycle::getTaskId, id));
    }

    @Override
    public void permanentDelete(Long id) {
        taskRecycleMapper.delete(new LambdaQueryWrapper<TaskRecycle>().eq(TaskRecycle::getTaskId, id));
        taskMapper.deleteById(id);
    }

    @Override
    public List<Task> getOverdueTasks(Long userId) {
        return taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .ne(Task::getStatus, 2)
                .lt(Task::getDueDate, LocalDateTime.now()));
    }
}
