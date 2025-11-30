import request from '../utils/request'

export const register = (data: { username: string; password: string; email?: string; phone?: string }) =>
  request.post('/auth/register', data)

export const login = (data: { username: string; password: string }) =>
  request.post('/auth/login', data)
