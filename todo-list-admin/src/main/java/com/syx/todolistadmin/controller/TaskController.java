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
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(required = false) Integer priority,
                                     @RequestParam(required = false) String sortBy) {
        return Result.success(taskService.list(userId, page, size, category, status, priority, sortBy));
    }

    @GetMapping("/search")
    public Result<java.util.List<Task>> search(@RequestParam Long userId, @RequestParam String keyword) {
        return Result.success(taskService.search(userId, keyword));
    }

    @GetMapping("/recycle")
    public Result<java.util.List<Task>> getRecycleBin(@RequestParam Long userId) {
        return Result.success(taskService.getRecycleBin(userId));
    }

    @PostMapping("/recycle/{id}/restore")
    public Result<Void> restore(@PathVariable Long id) {
        taskService.restore(id);
        return Result.success(null);
    }

    @DeleteMapping("/recycle/{id}")
    public Result<Void> permanentDelete(@PathVariable Long id) {
        taskService.permanentDelete(id);
        return Result.success(null);
    }

    @GetMapping("/overdue")
    public Result<java.util.List<Task>> getOverdueTasks(@RequestParam Long userId) {
        return Result.success(taskService.getOverdueTasks(userId));
    }
}
