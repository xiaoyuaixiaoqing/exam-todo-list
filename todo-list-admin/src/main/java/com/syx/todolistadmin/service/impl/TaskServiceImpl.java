package com.syx.todolistadmin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.syx.todolistadmin.dto.ExportTaskDTO;
import com.syx.todolistadmin.entity.Task;
import com.syx.todolistadmin.entity.TaskConflict;
import com.syx.todolistadmin.entity.TaskRecycle;
import com.syx.todolistadmin.entity.TeamRecycle;
import com.syx.todolistadmin.mapper.TaskConflictMapper;
import com.syx.todolistadmin.mapper.TaskMapper;
import com.syx.todolistadmin.mapper.TaskRecycleMapper;
import com.syx.todolistadmin.mapper.TeamRecycleMapper;
import com.syx.todolistadmin.service.TaskLogService;
import com.syx.todolistadmin.service.TaskService;
import com.syx.todolistadmin.service.TeamService;
import com.syx.todolistadmin.websocket.TaskWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final TeamRecycleMapper teamRecycleMapper;
    private final TaskLogService taskLogService;
    private final TaskWebSocketHandler webSocketHandler;
    private final TaskConflictMapper taskConflictMapper;
    private final StringRedisTemplate redisTemplate;
    private final TeamService teamService;

    @Override
    @CacheEvict(value = "tasks", key = "#result.id")
    public Task create(Task task) {
        // 权限检查：如果是团队任务，需要是团队成员或团队所有者
        if (task.getTeamId() != null) {
            boolean isMember = teamService.isMember(task.getTeamId(), task.getUserId());
            boolean isOwner = teamService.isOwner(task.getTeamId(), task.getUserId());
            if (!isMember && !isOwner) {
                throw new RuntimeException("无权在此团队中创建任务");
            }
        }
        taskMapper.insert(task);
        taskLogService.log(task.getId(), task.getUserId(), "CREATE", null, JSON.toJSONString(task));
        broadcast("TASK_CREATE", task);
        return task;
    }

    @Override
    @CacheEvict(value = "tasks", key = "#task.id")
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
        // 权限检查：如果是团队任务，需要是团队成员
        if (old != null && old.getTeamId() != null) {
            if (!teamService.isMember(old.getTeamId(), task.getUserId())) {
                throw new RuntimeException("无权限修改团队任务");
            }
        }
        taskMapper.updateById(task);
        Task updated = taskMapper.selectById(task.getId());

        taskLogService.log(task.getId(), task.getUserId(), "UPDATE", JSON.toJSONString(old), JSON.toJSONString(updated));
        broadcast("TASK_UPDATE", updated);
        return updated;
    }

    @Override
    @CacheEvict(value = "tasks", key = "#id")
    public void delete(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        // 权限检查：如果是团队任务，需要是团队成员
        if (task.getTeamId() != null) {
            if (!teamService.isMember(task.getTeamId(), task.getUserId())) {
                throw new RuntimeException("无权限删除团队任务");
            }
        }
        
        // 区分个人任务和团队任务，分别保存到不同的回收站表
        if (task.getTeamId() != null) {
            // 团队任务，保存到team_recycle表
            TeamRecycle teamRecycle = new TeamRecycle();
            teamRecycle.setTaskId(id);
            teamRecycle.setTeamId(task.getTeamId());
            teamRecycle.setDeletedBy(task.getUserId());
            teamRecycle.setExpireTime(LocalDateTime.now().plusDays(15));
            teamRecycleMapper.insert(teamRecycle);
        } else {
            // 个人任务，保存到task_recycle表
            TaskRecycle recycle = new TaskRecycle();
            recycle.setTaskId(id);
            recycle.setDeletedBy(task.getUserId());
            recycle.setExpireTime(LocalDateTime.now().plusDays(15));
            taskRecycleMapper.insert(recycle);
        }
        
        taskMapper.deleteById(id);

        taskLogService.log(id, task.getUserId(), "DELETE", JSON.toJSONString(task), null);
        broadcast("TASK_DELETE", task);
    }

    @Override
    @Cacheable(value = "tasks", key = "#id")
    public Task getById(Long id) {
        Task task = taskMapper.selectById(id);
        if (task != null) {
            String lockKey = "task:lock:" + id;
            String lockedUserId = redisTemplate.opsForValue().get(lockKey);
            task.setLockedBy(lockedUserId != null ? Long.parseLong(lockedUserId) : null);
        }
        return task;
    }

    private void checkPermission(Task task, Long userId) {
        if (task.getTeamId() != null && !teamService.isMember(task.getTeamId(), userId)) {
            throw new RuntimeException("无权限访问团队任务");
        }
    }

    @Override
    public IPage<Task> list(Long userId, Integer page, Integer size, String category, Integer status, Integer priority, String sortBy) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId).isNull(Task::getTeamId);
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
                .isNull(Task::getTeamId)
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
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        // 权限检查：如果是团队任务，需要是团队成员或团队所有者
        if (task.getTeamId() != null) {
            boolean isMember = teamService.isMember(task.getTeamId(), userId);
            boolean isOwner = teamService.isOwner(task.getTeamId(), userId);
            if (!isMember && !isOwner) {
                throw new RuntimeException("无权限锁定团队任务");
            }
        }
        String key = "task:lock:" + taskId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(userId), 5, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(success)) {
            task.setLockedBy(userId);
            broadcast("TASK_LOCK", task);
            return true;
        }
        return false;
    }

    @Override
    public void unlock(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        // 权限检查：如果是团队任务，需要是团队成员或团队所有者
        if (task.getTeamId() != null) {
            boolean isMember = teamService.isMember(task.getTeamId(), userId);
            boolean isOwner = teamService.isOwner(task.getTeamId(), userId);
            if (!isMember && !isOwner) {
                throw new RuntimeException("无权限解锁团队任务");
            }
        }
        String key = "task:lock:" + taskId;
        redisTemplate.delete(key);
        task.setLockedBy(null);
        broadcast("TASK_UNLOCK", task);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        ids.forEach(this::delete);
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        ids.forEach(id -> {
            Task task = taskMapper.selectById(id);
            if (task != null) {
                task.setStatus(status);
                taskMapper.updateById(task);
                taskLogService.log(id, task.getUserId(), "BATCH_UPDATE_STATUS", String.valueOf(task.getStatus()), String.valueOf(status));
                broadcast("TASK_UPDATE", task);
            }
        });
    }

    @Override
    public void importTasks(List<Task> tasks, Long userId) {
        tasks.forEach(task -> {
            task.setUserId(userId);
            taskMapper.insert(task);
        });
    }

    @Override
    public List<ExportTaskDTO> exportTasks(Long userId) {
        return taskMapper.selectList(new LambdaQueryWrapper<Task>().eq(Task::getUserId, userId))
                .stream()
                .map(ExportTaskDTO::fromTask)
                .toList();
    }

    @Override
    public IPage<Task> getTeamTasks(Long teamId, Integer page, Integer size) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getTeamId, teamId);
        wrapper.orderByDesc(Task::getCreateTime);
        IPage<Task> result = taskMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(task -> {
            String key = "task:lock:" + task.getId();
            String lockedUserId = redisTemplate.opsForValue().get(key);
            task.setLockedBy(lockedUserId != null ? Long.parseLong(lockedUserId) : null);
        });
        return result;
    }

    @Override
    public List<Task> getTeamRecycleBin(Long teamId) {
        // 从team_recycle表查询丢入回收站的任务
        List<TeamRecycle> recycles = teamRecycleMapper.selectList(
                new LambdaQueryWrapper<TeamRecycle>().eq(TeamRecycle::getTeamId, teamId));
        return recycles.stream()
                .map(r -> taskMapper.selectByIdIgnoreLogicDel(r.getTaskId()).stream().findFirst().orElse(null))
                .filter(t -> t != null && t.getIsDeleted() == 0 && t.getTeamId() != null && t.getTeamId().equals(teamId))
                .toList();
    }

    @Override
    public void permanentDeleteTeamTask(Long taskId, Long teamId) {
        teamRecycleMapper.delete(new LambdaQueryWrapper<TeamRecycle>()
                .eq(TeamRecycle::getTaskId, taskId)
                .eq(TeamRecycle::getTeamId, teamId));
        taskMapper.deleteById(taskId);
    }

    @Override
    public void restoreTeamTask(Long taskId, Long teamId) {
        Task task = taskMapper.selectByIdIgnoreLogicDel(taskId).stream()
                .filter(t -> t.getIsDeleted() == 0 && t.getTeamId() != null && t.getTeamId().equals(teamId))
                .findFirst().orElse(null);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        taskMapper.restoreTask(taskId);
        teamRecycleMapper.delete(new LambdaQueryWrapper<TeamRecycle>()
                .eq(TeamRecycle::getTaskId, taskId)
                .eq(TeamRecycle::getTeamId, teamId));
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
