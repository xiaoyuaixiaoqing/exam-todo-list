package com.syx.todolistadmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.syx.todolistadmin.common.Result;
import com.syx.todolistadmin.entity.Task;
import com.syx.todolistadmin.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public Result<Task> create(@RequestBody Task task) {
        return Result.success(taskService.create(task));
    }

    @PutMapping("/{id}")
    public Result<Task> update(@PathVariable Long id, @RequestBody Task task) {
        task.setId(id);
        return Result.success(taskService.update(task));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @GetMapping
    public Result<IPage<Task>> list(@RequestParam Long userId,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(taskService.list(userId, page, size));
    }
}
