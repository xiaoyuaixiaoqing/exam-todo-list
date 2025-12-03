import request from '../utils/request'

export interface Notification {
  id: number
  userId: number
  taskId: number
  type: string
  content: string
  status: string
  sendTime: string
}

// 获取通知列表
export const getNotifications = () => {
  return request.get<Notification[]>('/notifications')
}

// 获取未读通知数量
export const getUnreadCount = () => {
  return request.get<number>('/notifications/unread-count')
}

// 标记通知为已读
export const markAsRead = (id: number) => {
  return request.put(`/notifications/${id}/read`)
}

// 删除通知
export const deleteNotification = (id: number) => {
  return request.delete(`/notifications/${id}`)
}
