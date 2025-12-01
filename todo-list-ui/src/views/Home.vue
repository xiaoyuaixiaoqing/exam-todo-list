<template>
  <div class="home-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <el-button class="new-chat-btn" @click="openDialog()">
          <el-icon><EditPen /></el-icon>
          新建任务
        </el-button>
      </div>

      <div class="nav-items">
        <div class="nav-item" @click="$router.push('/recycle')">
          <el-icon><Delete /></el-icon>
          <span>回收站</span>
        </div>
        <div class="nav-item" @click="logout">
          <el-icon><User /></el-icon>
          <span>退出登录</span>
        </div>
      </div>
    </div>

    <div class="main-area">
      <div class="chat-header">
        <h1>待办事项</h1>
        <div class="header-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索任务..."
            @input="handleSearch"
            clearable
            size="default"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </div>

      <div class="filter-bar">
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
        <el-select v-model="filter.sortBy" @change="loadTasks" size="default">
          <el-option label="创建时间" value="createTime" />
          <el-option label="优先级" value="priority" />
          <el-option label="截止日期" value="dueDate" />
        </el-select>
      </div>

      <div class="chat-content">
        <el-alert v-if="overdueTasks.length > 0" type="warning" :closable="false" show-icon>
          您有 {{ overdueTasks.length }} 个任务已超期
          <el-button type="text" @click="viewOverdue" style="margin-left: 12px">立即查看</el-button>
        </el-alert>
        <el-alert v-if="soonDueTasks.length > 0" type="error" :closable="false" show-icon>
          您有 {{ soonDueTasks.length }} 个任务还有一天截止
          <el-button type="text" @click="viewSoonDue" style="margin-left: 12px">立即查看</el-button>
        </el-alert>

        <div class="message-list">
          <div v-for="task in tasks" :key="task.id" class="message-item">
            <TaskCard
              :task="task"
              @edit="openDialog"
              @delete="handleDelete"
              @toggle-status="toggleStatus"
            />
          </div>
        </div>

        <el-empty v-if="tasks.length === 0" description="暂无任务" />
      </div>
    </div>

    <TaskDialog
      :visible="showDialog"
      :task="currentTask"
      @close="closeDialog"
      @save="handleSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Delete, User, Search } from '@element-plus/icons-vue'
import { getTasks, createTask, updateTask, deleteTask, searchTasks, getOverdueTasks, lockTask, unlockTask } from '../api/task'
import { useUserStore } from '../stores/user'
import confetti from 'canvas-confetti'
import TaskCard from '../components/TaskCard.vue'
import TaskDialog from '../components/TaskDialog.vue'
import { TaskWebSocket } from '../utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const tasks = ref([])
const overdueTasks = ref([])
const showDialog = ref(false)
const searchKeyword = ref('')
const filter = reactive({ category: null, status: null, priority: null, sortBy: 'createTime' })
const currentTask = reactive({ id: null, title: '', description: '', category: '工作', status: 0, priority: 1, dueDate: null, userId: 1 })
const ws = new TaskWebSocket()

const soonDueTasks = computed(() => {
  const now = new Date().getTime()
  const oneDayMs = 86400000
  return tasks.value.filter((task: any) => {
    if (task.status === 2 || !task.dueDate) return false
    const dueTime = new Date(task.dueDate).getTime()
    return dueTime > now && dueTime - now <= oneDayMs
  })
})

const viewOverdue = () => {
  filter.category = null
  filter.priority = null
  filter.status = 4
  searchKeyword.value = ''
  loadTasks()
}

const viewSoonDue = () => {
  filter.category = null
  filter.priority = null
  filter.status = null
  filter.sortBy = 'dueDate'
  searchKeyword.value = ''
  loadTasks()
}

const loadTasks = async () => {
  const params: any = { userId: 1, page: 1, size: 100 }
  if (filter.category) params.category = filter.category
  if (filter.status !== null) params.status = filter.status
  if (filter.priority) params.priority = filter.priority
  if (filter.sortBy) params.sortBy = filter.sortBy
  const res: any = await getTasks(params)
  tasks.value = res.data.records
}

const handleSearch = async () => {
  if (searchKeyword.value) {
    const res: any = await searchTasks({ userId: 1, keyword: searchKeyword.value })
    tasks.value = res.data
  } else {
    loadTasks()
  }
}

const loadOverdueTasks = async () => {
  const res: any = await getOverdueTasks(1)
  overdueTasks.value = res.data
}

const openDialog = async (task?: any) => {
  if (task) {
    Object.assign(currentTask, task)
    if (task.id) {
      const res: any = await lockTask(task.id, 1)
      if (!res.data) {
        ElMessage.warning('任务正在被其他用户编辑')
        return
      }
    }
  } else {
    Object.assign(currentTask, { id: null, title: '', description: '', category: '工作', status: 0, priority: 1, dueDate: null, userId: 1 })
  }
  showDialog.value = true
}

const closeDialog = async () => {
  if (currentTask.id) {
    await unlockTask(currentTask.id, 1)
  }
  showDialog.value = false
}

const handleSave = async (task: any) => {
  if (task.id) {
    unlockTask(task.id, 1)
  }
  showDialog.value = false
  try {
    if (task.id) {
      await updateTask(task.id, task)
      ElMessage.success('更新成功')
    } else {
      await createTask(task)
      ElMessage.success('创建成功')
    }
    loadTasks()
  } catch (error: any) {
    if (error.response?.data?.message?.includes('冲突')) {
      ElMessageBox.alert('任务已被其他用户修改，请刷新后重试', '版本冲突', {
        confirmButtonText: '刷新',
        callback: () => loadTasks()
      })
    } else {
      ElMessage.error('操作失败')
    }
    loadTasks()
  }
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确认删除？')
    const index = tasks.value.findIndex((t: any) => t.id === id)
    const deletedTask = tasks.value[index]
    tasks.value.splice(index, 1)
    deleteTask(id).catch(() => {
      tasks.value.splice(index, 0, deletedTask)
      ElMessage.error('删除失败')
    })
    ElMessage.success('已移至回收站')
  } catch {}
}

const toggleStatus = async (task: any, status: number) => {
  const oldStatus = task.status
  task.status = status
  if (status === 2) {
    confetti({
      particleCount: 100,
      spread: 70,
      origin: { y: 0.6 }
    })
  }
  ElMessage.success(status === 2 ? '完成！！！' : '未完成！！！')
  updateTask(task.id, { ...task, status }).catch(() => {
    task.status = oldStatus
    ElMessage.error('操作失败')
  })
}

const logout = () => {
  userStore.clearToken()
  router.push('/login')
}

onMounted(() => {
  loadTasks()
  loadOverdueTasks()
  ws.connect()
  ws.onMessage((message: any) => {
    if (message.type === 'TASK_LOCK' || message.type === 'TASK_UNLOCK') {
      loadTasks()
    } else if (message.type === 'TASK_CREATE') {
      loadTasks()
    } else if (message.type === 'TASK_UPDATE') {
      loadTasks()
    } else if (message.type === 'TASK_DELETE') {
      loadTasks()
    }
  })
})

onUnmounted(() => {
  if (currentTask.id && showDialog.value) {
    unlockTask(currentTask.id, 1)
  }
  ws.disconnect()
})
</script>

<style scoped>
.home-container {
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

.header-search {
  width: 300px;
}

.header-search :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 0 0 1px #d4d4d4;
  padding: 8px 12px;
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
</style>
