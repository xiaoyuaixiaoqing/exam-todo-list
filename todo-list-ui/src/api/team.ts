import request from '@/utils/request'

export interface Team {
  id?: string
  name: string
  ownerId?: string
  description?: string
  createTime?: string
  updateTime?: string
}

export interface TeamMember {
  id?: string
  teamId: string
  userId: string
  role: number
  createTime?: string
}

export const createTeam = (data: Team) => {
  return request.post('/teams', data)
}

export const getTeam = (id: string | number) => {
  return request.get(`/teams/${id}`)
}

export const getMyTeams = () => {
  return request.get('/teams')
}

export const updateTeam = (id: string | number, data: Team) => {
  return request.put(`/teams/${id}`, data)
}

export const deleteTeam = (id: string | number) => {
  return request.delete(`/teams/${id}`)
}

export const addMember = (teamId: string | number, userId: string | number, role: number) => {
  return request.post(`/teams/${teamId}/members`, { userId, role })
}

export const removeMember = (teamId: string | number, userId: string | number) => {
  return request.delete(`/teams/${teamId}/members/${userId}`)
}

export const getMembers = (teamId: string | number) => {
  return request.get(`/teams/${teamId}/members`)
}

export const getOnlineMembers = (teamId: string | number) => {
  return request.get(`/teams/${teamId}/online-members`)
}

export const getTeamTasks = (teamId: string | number) => {
  return request.get(`/teams/${teamId}/tasks`)
}

export const getTeamRecycleBin = (teamId: string | number) => {
  return request.get(`/teams/${teamId}/recycle`)
}

export const permanentDeleteTeamTask = (teamId: string | number, taskId: string) => {
  return request.delete(`/teams/${teamId}/recycle/${taskId}`)
}

export const restoreTeamTask = (teamId: string | number, taskId: string) => {
  return request.post(`/teams/${teamId}/recycle/${taskId}/restore`)
}
