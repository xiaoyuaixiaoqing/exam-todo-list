import request from '../utils/request'

// 语音转任务
export const voiceToTask = (audioFile: File) => {
  const formData = new FormData()
  formData.append('audio', audioFile)
  return request.post('/api/ai/voice-to-task', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 智能分类
export const classifyTask = (title: string, description?: string) => {
  return request.post<{
    category: string
    priority: string
    reason: string
  }>('/api/ai/classify', { title, description })
}
