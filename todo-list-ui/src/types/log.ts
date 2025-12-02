export interface TaskLog {
  id: string
  action: string
  oldValue?: string
  newValue?: string
  createTime: string
}
