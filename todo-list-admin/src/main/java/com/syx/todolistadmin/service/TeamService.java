package com.syx.todolistadmin.service;

import com.syx.todolistadmin.entity.Team;
import com.syx.todolistadmin.entity.TeamMember;
import java.util.List;

public interface TeamService {
    Team createTeam(Team team, Long userId);
    Team getTeamById(Long id);
    List<Team> getMyTeams(Long userId);
    Team updateTeam(Long id, Team team, Long userId);
    void deleteTeam(Long id, Long userId);
    void addMember(Long teamId, Long userId, Integer role, Long operatorId);
    void removeMember(Long teamId, Long userId, Long operatorId);
    List<TeamMember> getTeamMembers(Long teamId);
    List<TeamMember> getOnlineMembers(Long teamId);
    boolean isMember(Long teamId, Long userId);
    boolean isOwner(Long teamId, Long userId);
}
