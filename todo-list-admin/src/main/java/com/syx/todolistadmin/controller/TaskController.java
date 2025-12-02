package com.syx.todolistadmin.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.syx.todolistadmin.common.Result;
import com.syx.todolistadmin.dto.ExportTaskDTO;
import com.syx.todolistadmin.entity.Task;
import com.syx.todolistadmin.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.syx.todolistadmin.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public Result<Task> create(@RequestBody Task task, @RequestHeader(value = "Authorization", required = false) String token) {
        // 从 JWT Token 中提取真实的用户 ID，不依赖前端发送的 userId
        if (token != null && !token.isEmpty()) {
            try {
                Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
                task.setUserId(userId);
            } catch (Exception e) {
                throw new RuntimeException("无效的 Token，无法创建任务");
            }
        } else {
            throw new RuntimeException("缺少 Authorization 认证，无法创建任务");
        }
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
    public Result<IPage<Task>> list(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) Integer status,
                                    @RequestParam(required = false) Integer priority,
                                    @RequestParam(required = false) String sortBy) {
        Long userId = extractUserIdFromToken(token);
        return Result.success(taskService.list(userId, page, size, category, status, priority, sortBy));
    }

    @GetMapping("/search")
    public Result<List<Task>> search(@RequestHeader(value = "Authorization", required = false) String token, 
                                     @RequestParam String keyword) {
        Long userId = extractUserIdFromToken(token);
        return Result.success(taskService.search(userId, keyword));
    }

    @GetMapping("/recycle")
    public Result<List<Task>> getRecycleBin(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = extractUserIdFromToken(token);
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
    public Result<List<Task>> getOverdueTasks(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = extractUserIdFromToken(token);
        return Result.success(taskService.getOverdueTasks(userId));
    }

    @PostMapping("/{id}/lock")
    public Result<Boolean> lock(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = extractUserIdFromToken(token);
        return Result.success(taskService.lock(id, userId));
    }

    @PostMapping("/{id}/unlock")
    public Result<Void> unlock(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = extractUserIdFromToken(token);
        taskService.unlock(id, userId);
        return Result.success(null);
    }

    @PostMapping("/batch/delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        taskService.batchDelete(ids);
        return Result.success(null);
    }

    @PostMapping("/batch/status")
    public Result<Void> batchUpdateStatus(@RequestBody Map<String, Object> params) {
        List<Long> ids = (List<Long>) params.get("ids");
        Integer status = (Integer) params.get("status");
        taskService.batchUpdateStatus(ids, status);
        return Result.success(null);
    }

    @PostMapping("/import")
    public Result<Void> importTasks(@RequestParam("file") MultipartFile file, @RequestHeader(value = "Authorization", required = false) String token) throws IOException {
        Long userId = extractUserIdFromToken(token);
        List<Task> tasks = EasyExcel.read(file.getInputStream()).head(Task.class).sheet().doReadSync();
        taskService.importTasks(tasks, userId);
        return Result.success(null);
    }

    @GetMapping("/export")
    public void exportTasks(@RequestHeader(value = "Authorization", required = false) String token, HttpServletResponse response) throws IOException {
        Long userId = extractUserIdFromToken(token);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("任务列表", StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), ExportTaskDTO.class).sheet("任务").doWrite(taskService.exportTasks(userId));
    }

    @PostMapping("/team/{id}/lock")
    public Result<Boolean> lockTeamTask(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = extractUserIdFromToken(token);
        return Result.success(taskService.lock(id, userId));
    }

    @PostMapping("/team/{id}/unlock")
    public Result<Void> unlockTeamTask(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = extractUserIdFromToken(token);
        taskService.unlock(id, userId);
        return Result.success(null);
    }

    /**
     * 从 JWT Token 中提取用户 ID
     */
    private Long extractUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("缺少 Authorization 认证");
        }
        try {
            return jwtUtil.getUserId(token.replace("Bearer ", ""));
        } catch (Exception e) {
            throw new RuntimeException("无效的 Token");
        }
    }
}