package com.syx.todolistadmin.controller;

import com.syx.todolistadmin.common.Result;
import com.syx.todolistadmin.entity.Team;
import com.syx.todolistadmin.entity.TeamMember;
import com.syx.todolistadmin.service.TeamService;
import com.syx.todolistadmin.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.syx.todolistadmin.service.TaskService;
import com.syx.todolistadmin.entity.Task;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;
    private final JwtUtil jwtUtil;
    private final TaskService taskService;

    @PostMapping
    public Result<Team> createTeam(@RequestBody Team team, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(teamService.createTeam(team, userId));
    }

    @GetMapping("/{id}")
    public Result<Team> getTeam(@PathVariable Long id) {
        return Result.success(teamService.getTeamById(id));
    }

    @GetMapping
    public Result<List<Team>> getMyTeams(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(teamService.getMyTeams(userId));
    }

    @PutMapping("/{id}")
    public Result<Team> updateTeam(@PathVariable Long id, @RequestBody Team team,
                                    @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return Result.success(teamService.updateTeam(id, team, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTeam(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        teamService.deleteTeam(id, userId);
        return Result.success(null);
    }

    @PostMapping("/{id}/members")
    public Result<Void> addMember(@PathVariable Long id, @RequestBody Map<String, Object> params,
                                   @RequestHeader("Authorization") String token) {
        Long operatorId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer role = Integer.valueOf(params.get("role").toString());
        teamService.addMember(id, userId, role, operatorId);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId,
                                      @RequestHeader("Authorization") String token) {
        Long operatorId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        teamService.removeMember(id, userId, operatorId);
        return Result.success(null);
    }

    @GetMapping("/{id}/members")
    public Result<List<TeamMember>> getMembers(@PathVariable Long id) {
        return Result.success(teamService.getTeamMembers(id));
    }

    @GetMapping("/{id}/online-members")
    public Result<List<TeamMember>> getOnlineMembers(@PathVariable Long id) {
        return Result.success(teamService.getOnlineMembers(id));
    }

    @GetMapping("/{id}/tasks")
    public Result<IPage<Task>> getTeamTasks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(taskService.getTeamTasks(id, page, size));
    }

    @GetMapping("/{id}/recycle")
    public Result<List<Task>> getTeamRecycleBin(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        // 检查是否是团队串主
        if (!teamService.isOwner(id, userId)) {
            throw new RuntimeException("无权限访问团队回收站");
        }
        return Result.success(taskService.getTeamRecycleBin(id));
    }

    @DeleteMapping("/{teamId}/recycle/{taskId}")
    public Result<Void> permanentDeleteTeamTask(
            @PathVariable Long teamId,
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        // 检查是否是团队串主
        if (!teamService.isOwner(teamId, userId)) {
            throw new RuntimeException("无权限永久删除团队任务");
        }
        taskService.permanentDeleteTeamTask(taskId, teamId);
        return Result.success(null);
    }

    @PostMapping("/{teamId}/recycle/{taskId}/restore")
    public Result<Void> restoreTeamTask(
            @PathVariable Long teamId,
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        // 检查是否是团队串主
        if (!teamService.isOwner(teamId, userId)) {
            throw new RuntimeException("无权限恢复团队任务");
        }
        taskService.restoreTeamTask(taskId, teamId);
        return Result.success(null);
    }
}
