import request from '../utils/request'

export const getTasks = (params: any) =>
  request.get('/tasks', { params })

export const getTask = (id: string) =>
  request.get(`/tasks/${id}`)

export const createTask = (data: any) =>
  request.post('/tasks', data)

export const updateTask = (id: string, data: any) =>
  request.put(`/tasks/${id}`, data)

export const deleteTask = (id: string) =>
  request.delete(`/tasks/${id}`)

export const searchTasks = (params: { userId: number; keyword: string }) =>
  request.get('/tasks/search', { params })

export const getRecycleBin = (userId: number) =>
  request.get('/tasks/recycle', { params: { userId } })

export const restoreTask = (id: string) =>
  request.post(`/tasks/recycle/${id}/restore`)

export const permanentDeleteTask = (id: string) =>
  request.delete(`/tasks/recycle/${id}`)

export const getOverdueTasks = (userId: number) =>
  request.get('/tasks/overdue', { params: { userId } })

export const lockTask = (id: string, userId: number) =>
  request.post(`/tasks/${id}/lock`, null, { params: { userId } })

export const unlockTask = (id: string, userId: number) =>
  request.post(`/tasks/${id}/unlock`, null, { params: { userId } })
