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

export const searchTasks = (keyword: string) =>
  request.get('/tasks/search', { params: { keyword } })

export const getRecycleBin = () =>
  request.get('/tasks/recycle')

export const restoreTask = (id: string) =>
  request.post(`/tasks/recycle/${id}/restore`)

export const permanentDeleteTask = (id: string) =>
  request.delete(`/tasks/recycle/${id}`)

export const getOverdueTasks = () =>
  request.get('/tasks/overdue')

export const lockTask = (id: string) =>
  request.post(`/tasks/${id}/lock`)

export const unlockTask = (id: string) =>
  request.post(`/tasks/${id}/unlock`)

export const batchDeleteTasks = (ids: number[]) =>
  request.post('/tasks/batch/delete', ids)

export const batchUpdateStatus = (ids: number[], status: number) =>
  request.post('/tasks/batch/status', { ids, status })

export const importTasks = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/tasks/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const exportTasks = () =>
  request.get('/tasks/export', { responseType: 'blob' })

export const lockTeamTask = (id: string) =>
  request.post(`/tasks/team/${id}/lock`)

export const unlockTeamTask = (id: string) =>
  request.post(`/tasks/team/${id}/unlock`)
