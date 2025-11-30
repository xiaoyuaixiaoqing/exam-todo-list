import request from '../utils/request'

export const getTasks = (params: { userId: number; page?: number; size?: number }) =>
  request.get('/tasks', { params })

export const getTask = (id: number) =>
  request.get(`/tasks/${id}`)

export const createTask = (data: any) =>
  request.post('/tasks', data)

export const updateTask = (id: number, data: any) =>
  request.put(`/tasks/${id}`, data)

export const deleteTask = (id: number) =>
  request.delete(`/tasks/${id}`)
