<template>
  <div class="team-task-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <el-button class="new-chat-btn" @click="openDialog()">
          <el-icon><EditPen /></el-icon>
          新建任务
        </el-button>
      </div>

      <div class="nav-items">
        <div class="nav-item" @click="$router.push('/teams')">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回团队</span>
        </div>
        <div class="nav-item" @click="$router.push('/home')">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回主界面</span>
        </div>
      </div>
    </div>

    <div class="main-area">
      <div class="chat-header">
        <h1>{{ teamName }} - 待办事项</h1>
        <div class="header-right">
          <div class="online-info">
            <el-badge :value="onlineCount" class="online-badge">
              <el-button plain size="small">在线成员</el-button>
            </el-badge>
            <el-popover placement="bottom" :width="200" trigger="hover">
              <template #reference>
                <el-button link type="primary" size="small">查看详情</el-button>
              </template>
              <div class="online-members-list">
                <div v-if="onlineMembers.length === 0" class="empty-text">暂无在线成员</div>
                <div v-for="member in onlineMembers" :key="member.id" class="member-item">
                  <el-icon class="online-icon"><CircleCheckFilled /></el-icon>
                  <span>{{ member.userId }}</span>
                </div>
              </div>
            </el-popover>
          </div>
          <el-button @click="goToRecycleBin" size="default">
            <el-icon><Delete /></el-icon>
            回收站
          </el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-checkbox v-model="batchMode" @change="handleBatchModeChange">批量操作</el-checkbox>
        <el-select v-model="filter.category" placeholder="分类" clearable @change="loadTasks" size="default">
          <el-option label="工作" value="工作" />
          <el-option label="学习" value="学习" />
          <el-option label="生活" value="生活" />
        </el-select>
        <el-select v-model="filter.status" placeholder="状态" clearable @change="loadTasks" size="default">
          <el-option label="待办" :value="0" />
          <el-option label="未完成" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已超期" :value="4" />
        </el-select>
        <el-select v-model="filter.priority" placeholder="优先级" clearable @change="loadTasks" size="default">
          <el-option label="低" :value="1" />
          <el-option label="中" :value="2" />
          <el-option label="高" :value="3" />
        </el-select>
      </div>

      <div v-if="batchMode && selectedIds.length > 0" class="batch-actions">
        <span>已选择 {{ selectedIds.length }} 项</span>
        <el-button size="default" type="primary" @click="handleBatchStatus(2)">标记完成</el-button>
        <el-button size="default" @click="handleBatchStatus(0)">标记待办</el-button>
        <el-button size="default" type="danger" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <div class="chat-content">
        <div v-if="isLoading" class="skeleton-list">
          <div v-for="index in 3" :key="index" class="skeleton-item">
            <div class="skeleton-avatar"></div>
            <div class="skeleton-content">
              <div class="skeleton-title"></div>
              <div class="skeleton-text"></div>
            </div>
          </div>
        </div>

        <div v-else class="message-list">
          <div v-for="task in tasks" :key="task.id" class="message-item">
            <el-checkbox v-if="batchMode" v-model="selectedIds" :value="task.id" style="margin-right: 12px" />
            <TaskCard
              :task="task"
              @edit="openDialog"
              @delete="handleDelete"
              @toggle-status="toggleStatus"
            />
          </div>
        </div>

        <el-empty v-if="!isLoading && tasks.length === 0" description="暂无任务" />
      </div>
    </div>

    <TaskDialog
      :visible="showDialog"
      :task="currentTask"
      :team-id="currentTeamId"
      @close="closeDialog"
      @save="handleSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, ArrowLeft, Delete, CircleCheckFilled } from '@element-plus/icons-vue'
import { getTeamTasks, getTeam, getOnlineMembers, type Team } from '../api/team'
import { lockTask, unlockTask, lockTeamTask, unlockTeamTask, updateTask as apiUpdateTask, deleteTask as apiDeleteTask, createTask as apiCreateTask, batchUpdateStatus as apiBatchUpdateStatus, batchDeleteTasks as apiBatchDeleteTasks } from '../api/task'
import TaskCard from '../components/TaskCard.vue'
import TaskDialog from '../components/TaskDialog.vue'
import type { Task } from '../types/task'
import { TaskWebSocket } from '../utils/websocket'

const router = useRouter()
const route = useRoute()
const currentTeamId = ref<string | number>(route.params.id as string)
const teamName = ref('团队')
const tasks = ref<Task[]>([])
const showDialog = ref(false)
const isLoading = ref(true)
const batchMode = ref(false)
const selectedIds = ref<number[]>([])
const filter = reactive({ category: null, status: null, priority: null })
const currentTask = reactive({ id: null, title: '', description: '', category: '工作', status: 0, priority: 1, dueDate: null, userId: 1, teamId: currentTeamId.value })
const onlineMembers = ref<any[]>([])
const onlineCount = ref(0)
const ws = new TaskWebSocket()

const loadTeam = async () => {
  try {
    const res = await getTeam(currentTeamId.value)
    teamName.value = res.data?.name || '团队'
  } catch (error) {
    ElMessage.error('获取团队信息失败')
  }
}

const loadOnlineMembers = async () => {
  try {
    const res: any = await getOnlineMembers(currentTeamId.value)
    const members = res.data || []
    onlineMembers.value = members.filter((m: any) => m.isOnline)
    onlineCount.value = onlineMembers.value.length
  } catch (error) {
    console.error('加载在线成员失败', error)
  }
}

const loadTasks = async () => {
  try {
    isLoading.value = true
    const res: any = await getTeamTasks(currentTeamId.value)
    let taskList = res.data?.records || res.data || []
    
    if (filter.category) {
      taskList = taskList.filter((t: any) => t.category === filter.category)
    }
    if (filter.status !== null) {
      taskList = taskList.filter((t: any) => t.status === filter.status)
    }
    if (filter.priority) {
      taskList = taskList.filter((t: any) => t.priority === filter.priority)
    }
    
    tasks.value = taskList
  } catch (error) {
    ElMessage.error('加载任务失败')
  } finally {
    isLoading.value = false
  }
}

const openDialog = async (task?: any) => {
  if (task) {
    // 先检查任务是否已被锁定
    const res: any = await lockTeamTask(task.id)
    if (!res.data) {
      ElMessage.warning('任务正在被其他用户编辑')
      // 重新加载任务以获取最新的锁定状态
      await loadTasks()
      return
    }
    Object.assign(currentTask, task)
  } else {
    Object.assign(currentTask, { id: null, title: '', description: '', category: '工作', status: 0, priority: 1, dueDate: null, userId: 1, teamId: currentTeamId.value })
  }
  showDialog.value = true
}

const closeDialog = async () => {
  if (currentTask.id) {
    await unlockTeamTask(currentTask.id)
    // 更新单个任务的锁定状态而不是刷新整个列表
    const taskIndex = tasks.value.findIndex((t: any) => t.id === currentTask.id)
    if (taskIndex !== -1) {
      tasks.value[taskIndex].lockedBy = null
    }
  }
  showDialog.value = false
}

const handleSave = async (task: any) => {
  if (task.id) {
    await unlockTeamTask(task.id)
    // 更新单个任务的锁定状态
    const taskIndex = tasks.value.findIndex((t: any) => t.id === task.id)
    if (taskIndex !== -1) {
      tasks.value[taskIndex].lockedBy = null
    }
  }
  showDialog.value = false
  try {
    task.teamId = currentTeamId.value
    if (task.id) {
      const res = await apiUpdateTask(task.id, task)
      ElMessage.success('更新成功')
      // 更新单个任务而不是刷新整个列表
      const taskIndex = tasks.value.findIndex((t: any) => t.id === task.id)
      if (taskIndex !== -1) {
        Object.assign(tasks.value[taskIndex], res.data)
      }
    } else {
      const res = await apiCreateTask(task)
      ElMessage.success('创建成功')
      // 添加新任务到列表顶部
      tasks.value.unshift(res.data)
    }
  } catch (error: any) {
    ElMessage.error('操作失败')
    // 只在需要时才刷新整个列表
    await loadTasks()
  }
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确认删除？')
    const index = tasks.value.findIndex((t: any) => t.id === id)
    const deletedTask = tasks.value[index]
    tasks.value.splice(index, 1)
    await apiDeleteTask(id)
    ElMessage.success('已移至回收站')
  } catch (error) {
    // 如果删除失败，将任务恢复到原位置
    const index = tasks.value.findIndex((t: any) => t.id === id)
    if (index === -1) {
      tasks.value.splice(index, 0, deletedTask)
    }
    ElMessage.error('删除失败')
  }
}

const toggleStatus = async (task: any, status: number) => {
  const oldStatus = task.status
  task.status = status
  ElMessage.success(status === 2 ? '完成！！！' : '未完成！！！')
  try {
    const res = await apiUpdateTask(task.id, { ...task, status })
    // 更新单个任务而不是刷新整个列表
    const taskIndex = tasks.value.findIndex((t: any) => t.id === task.id)
    if (taskIndex !== -1) {
      Object.assign(tasks.value[taskIndex], res.data)
    }
  } catch (error) {
    task.status = oldStatus
    ElMessage.error('操作失败')
  }
}

const handleBatchModeChange = () => {
  selectedIds.value = []
}

const handleBatchStatus = async (status: number) => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择任务')
    return
  }
  try {
    await apiBatchUpdateStatus(selectedIds.value, status)
    ElMessage.success('批量更新成功')
    selectedIds.value = []
    batchMode.value = false
    await loadTasks()
  } catch (error) {
    ElMessage.error('批量更新失败')
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择任务')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个任务吗？`, '警告', { type: 'warning' })
    await apiBatchDeleteTasks(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    batchMode.value = false
    await loadTasks()
  } catch {}
}

const goToRecycleBin = () => {
  router.push(`/teams/${currentTeamId.value}/recycle`)
}

onMounted(() => {
  loadTeam()
  loadTasks()
  loadOnlineMembers()
  ws.connect()
  ws.onMessage((message: any) => {
    if (message.type === 'TASK_LOCK' || message.type === 'TASK_UNLOCK' || message.type === 'TASK_UPDATE') {
      // 只在对话框未打开时才刷新任务列表，避免编辑时的闪烁
      if (!showDialog.value) {
        // 对于锁相关消息，只更新对应任务的状态
        if ((message.type === 'TASK_LOCK' || message.type === 'TASK_UNLOCK') && message.data) {
          const taskIndex = tasks.value.findIndex((t: any) => t.id === message.data.id)
          if (taskIndex !== -1) {
            tasks.value[taskIndex].lockedBy = message.data.lockedBy
          }
        } 
        // 对于更新消息，更新对应任务
        else if (message.type === 'TASK_UPDATE' && message.data) {
          const taskIndex = tasks.value.findIndex((t: any) => t.id === message.data.id)
          if (taskIndex !== -1) {
            Object.assign(tasks.value[taskIndex], message.data)
          } else {
            // 如果是新任务，添加到列表
            tasks.value.unshift(message.data)
          }
        }
        // 其他情况刷新整个列表
        else {
          loadTasks()
        }
      }
    }
  })
  setInterval(() => {
    loadOnlineMembers()
  }, 30000)
})

onUnmounted(() => {
  ws.disconnect()
})
</script>

<style scoped>
.team-task-container {
  display: flex;
  height: 100vh;
  background: #ffffff;
}

.sidebar {
  width: 260px;
  background: #f9f9f9;
  border-right: 1px solid #ececec;
  display: flex;
  flex-direction: column;
  padding: 12px;
}

.sidebar-header {
  margin-bottom: 8px;
}

.new-chat-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  border: 1px solid #d1d1d1;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  color: #202123;
  cursor: pointer;
  transition: all 0.2s;
}

.new-chat-btn:hover {
  background: #ececec;
}

.nav-items {
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #202123;
  transition: background 0.2s;
}

.nav-item:hover {
  background: #ececec;
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #ececec;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.chat-header h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #202123;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.online-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.online-badge :deep(.el-badge__content) {
  background-color: #67c23a;
  color: white;
}

.online-members-list {
  max-height: 300px;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
}

.online-icon {
  color: #67c23a;
  font-size: 16px;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 16px 0;
  font-size: 14px;
}

.filter-bar {
  padding: 12px 24px;
  border-bottom: 1px solid #ececec;
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-bar .el-select {
  width: 140px;
}

.filter-bar :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 0 0 1px #d4d4d4;
  padding: 8px 12px;
}

.batch-actions {
  padding: 12px 24px;
  border-bottom: 1px solid #ececec;
  display: flex;
  gap: 12px;
  align-items: center;
  background: #f5f5f5;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.message-list {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message-item {
  display: flex;
  gap: 16px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #d1d1d1;
  padding: 6px 16px;
}

:deep(.el-alert) {
  border-radius: 12px;
  margin-bottom: 24px;
}

/* 骨架屏样式 */
.skeleton-list {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.skeleton-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #ececec;
  animation: fadeIn 0.6s ease-in-out;
}

.skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-title {
  height: 20px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  width: 60%;
}

.skeleton-text {
  height: 16px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
