package com.syx.todolistadmin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syx.todolistadmin.entity.Task;
import com.syx.todolistadmin.entity.TaskConflict;
import com.syx.todolistadmin.entity.TaskRecycle;
import com.syx.todolistadmin.mapper.TaskConflictMapper;
import com.syx.todolistadmin.mapper.TaskMapper;
import com.syx.todolistadmin.mapper.TaskRecycleMapper;
import com.syx.todolistadmin.service.TaskLogService;
import com.syx.todolistadmin.service.TaskService;
import com.syx.todolistadmin.websocket.TaskWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskMapper taskMapper;
    private final TaskRecycleMapper taskRecycleMapper;
    private final TaskLogService taskLogService;
    private final TaskWebSocketHandler webSocketHandler;
    private final TaskConflictMapper taskConflictMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Task create(Task task) {
        taskMapper.insert(task);
        taskLogService.log(task.getId(), task.getUserId(), "CREATE", null, JSON.toJSONString(task));
        broadcast("TASK_CREATE", task);
        return task;
    }

    @Override
    public Task update(Task task) {
        Task old = taskMapper.selectById(task.getId());
        if (old != null && !old.getVersion().equals(task.getVersion())) {
            TaskConflict conflict = new TaskConflict();
            conflict.setTaskId(task.getId());
            conflict.setUserId(task.getUserId());
            conflict.setConflictData(JSON.toJSONString(task));
            taskConflictMapper.insert(conflict);
            throw new RuntimeException("任务版本冲突");
        }
        taskMapper.updateById(task);
        Task updated = taskMapper.selectById(task.getId());
        taskLogService.log(task.getId(), task.getUserId(), "UPDATE", JSON.toJSONString(old), JSON.toJSONString(updated));
        broadcast("TASK_UPDATE", updated);
        return updated;
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
        taskLogService.log(id, task.getUserId(), "DELETE", JSON.toJSONString(task), null);
        broadcast("TASK_DELETE", task);
    }

    @Override
    public Task getById(Long id) {
        Task task = taskMapper.selectById(id);
        if (task != null) {
            String key = "task:lock:" + id;
            String lockedUserId = redisTemplate.opsForValue().get(key);
            task.setLockedBy(lockedUserId != null ? Long.parseLong(lockedUserId) : null);
        }
        return task;
    }

    @Override
    public IPage<Task> list(Long userId, Integer page, Integer size, String category, Integer status, Integer priority, String sortBy) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<Task>().eq(Task::getUserId, userId);
        if (category != null) wrapper.eq(Task::getCategory, category);
        if (status != null) wrapper.eq(Task::getStatus, status);
        if (priority != null) wrapper.eq(Task::getPriority, priority);
        if ("priority".equals(sortBy)) wrapper.orderByDesc(Task::getPriority);
        else if ("dueDate".equals(sortBy)) wrapper.orderByAsc(Task::getDueDate);
        else wrapper.orderByDesc(Task::getCreateTime);
        IPage<Task> result = taskMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(task -> {
            String key = "task:lock:" + task.getId();
            String lockedUserId = redisTemplate.opsForValue().get(key);
            task.setLockedBy(lockedUserId != null ? Long.parseLong(lockedUserId) : null);
        });
        return result;
    }

    @Override
    public List<Task> search(Long userId, String keyword) {
        List<Task> tasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .and(w -> w.like(Task::getTitle, keyword).or().like(Task::getDescription, keyword)));
        tasks.forEach(task -> {
            String key = "task:lock:" + task.getId();
            String lockedUserId = redisTemplate.opsForValue().get(key);
            task.setLockedBy(lockedUserId != null ? Long.parseLong(lockedUserId) : null);
        });
        return tasks;
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
        taskLogService.log(id, task.getUserId(), "RESTORE", null, JSON.toJSONString(task));
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

    @Override
    public boolean lock(Long taskId, Long userId) {
        String key = "task:lock:" + taskId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(userId), 5, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(success)) {
            Task task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setLockedBy(userId);
                broadcast("TASK_LOCK", task);
            }
            return true;
        }
        return false;
    }

    @Override
    public void unlock(Long taskId, Long userId) {
        String key = "task:lock:" + taskId;
        redisTemplate.delete(key);
        Task task = taskMapper.selectById(taskId);
        if (task != null) {
            task.setLockedBy(null);
            broadcast("TASK_UNLOCK", task);
        }
    }

    private void broadcast(String type, Task task) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("data", task);
        String json = JSON.toJSONString(message);
        System.out.println("广播消息: " + json);
        webSocketHandler.broadcast(json);
    }
}
