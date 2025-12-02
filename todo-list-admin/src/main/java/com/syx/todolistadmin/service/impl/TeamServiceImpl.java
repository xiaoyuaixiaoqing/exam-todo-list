package com.syx.todolistadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.syx.todolistadmin.entity.Team;
import com.syx.todolistadmin.entity.TeamMember;
import com.syx.todolistadmin.mapper.TeamMapper;
import com.syx.todolistadmin.mapper.TeamMemberMapper;
import com.syx.todolistadmin.service.TeamService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final StringRedisTemplate redisTemplate;

    public TeamServiceImpl(TeamMapper teamMapper, TeamMemberMapper teamMemberMapper, StringRedisTemplate redisTemplate) {
        this.teamMapper = teamMapper;
        this.teamMemberMapper = teamMemberMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public Team createTeam(Team team, Long userId) {
        team.setOwnerId(userId);
        teamMapper.insert(team);
        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole(1);
        teamMemberMapper.insert(member);
        return team;
    }

    @Override
    public Team getTeamById(Long id) {
        return teamMapper.selectById(id);
    }

    @Override
    public List<Team> getMyTeams(Long userId) {
        List<TeamMember> members = teamMemberMapper.selectList(
            new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getUserId, userId)
        );
        return members.stream()
            .map(m -> teamMapper.selectById(m.getTeamId()))
            .filter(t -> t != null)
            .toList();
    }

    @Override
    public Team updateTeam(Long id, Team team, Long userId) {
        Team existing = teamMapper.selectById(id);
        if (existing == null || !existing.getOwnerId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        team.setId(id);
        teamMapper.updateById(team);
        return teamMapper.selectById(id);
    }

    @Override
    public void deleteTeam(Long id, Long userId) {
        Team team = teamMapper.selectById(id);
        if (team == null || !team.getOwnerId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        teamMapper.deleteById(id);
        teamMemberMapper.delete(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, id));
    }

    @Override
    public void addMember(Long teamId, Long userId, Integer role, Long operatorId) {
        if (!isOwner(teamId, operatorId)) {
            throw new RuntimeException("无权限");
        }
        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setRole(role);
        teamMemberMapper.insert(member);
    }

    @Override
    public void removeMember(Long teamId, Long userId, Long operatorId) {
        if (!isOwner(teamId, operatorId)) {
            throw new RuntimeException("无权限");
        }
        teamMemberMapper.delete(
            new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId)
        );
    }

    @Override
    public List<TeamMember> getTeamMembers(Long teamId) {
        return teamMemberMapper.selectList(
            new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, teamId)
        );
    }

    @Override
    public List<TeamMember> getOnlineMembers(Long teamId) {
        List<TeamMember> members = getTeamMembers(teamId);
        // 为每个成员设置在线状态，通过检查Redis中的在线记录
        members.forEach(member -> {
            String onlineKey = "user:online:" + member.getUserId();
            Boolean isOnline = redisTemplate.hasKey(onlineKey);
            member.setIsOnline(isOnline != null && isOnline);
        });
        return members;
    }

    @Override
    public boolean isMember(Long teamId, Long userId) {
        return teamMemberMapper.selectCount(
            new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId)
        ) > 0;
    }

    @Override
    public boolean isOwner(Long teamId, Long userId) {
        Team team = teamMapper.selectById(teamId);
        return team != null && team.getOwnerId().equals(userId);
    }
}
