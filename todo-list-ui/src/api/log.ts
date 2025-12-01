import request from '../utils/request'

export const getTaskLogs = (taskId: string) =>
  request.get(`/logs/tasks/${taskId}`)

export const getUserLogs = (userId: number) =>
  request.get(`/logs/users/${userId}`)

export const rollbackToLog = (taskId: string, logId: number, userId: number) =>
  request.post(`/logs/tasks/${taskId}/rollback/${logId}`, null, { params: { userId } })
