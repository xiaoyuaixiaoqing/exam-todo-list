export interface Task {
  id: string
  title: string
  description?: string
  category: string
  status: number
  priority: number
  dueDate?: string | null
  userId: number
  version?: number
  lockedBy?: string | null
}