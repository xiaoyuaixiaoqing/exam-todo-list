<template>
  <div class="home-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <el-button class="new-chat-btn" @click="showDialog = true">
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
        <el-select class="" v-model="filter.category" placeholder="分类" clearable @change="loadTasks" size="default">
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
            <div class="message-content" :class="'status-' + task.status">
              <div class="breathing-light" :class="'priority-' + task.priority"></div>
              <div v-if="task.status === 1" class="ribbon ribbon-incomplete">未完成</div>
              <div v-if="task.status === 2" class="ribbon ribbon-complete">已完成</div>
              <div v-if="isOverdue(task)" class="ribbon ribbon-overdue">已超期</div>
              <div class="task-header">
                <h3>{{ task.title }}</h3>
              </div>
              <p class="task-desc">{{ task.description || '暂无描述' }}</p>
              <div class="task-meta-wrapper">
                <div class="task-meta">
                  <el-tag size="small" effect="plain">{{ task.category }}</el-tag>
                  <span class="task-date" v-if="task.dueDate">
                    <el-icon><Clock /></el-icon>
                    {{ formatDate(task.dueDate) }}
                  </span>
                </div>
                <div class="task-status-actions" v-if="task.status === 0">
                  <button class="status-btn complete" @click="toggleStatus(task, 2)">
                    <el-icon><Select /></el-icon>
                    完成
                  </button>
                  <button class="status-btn incomplete" @click="toggleStatus(task, 1)">
                    <el-icon><Close /></el-icon>
                    未完成
                  </button>
                </div>
              </div>
              <div class="task-actions">
                <button class="action-btn" @click="editTask(task)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </button>
                <button class="action-btn delete" @click="handleDelete(task.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </button>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="tasks.length === 0" description="暂无任务" />
      </div>
    </div>

    <el-dialog v-model="showDialog" :title="currentTask.id ? '编辑任务' : '新建任务'" width="500px">
      <el-form :model="currentTask" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="currentTask.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentTask.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="currentTask.category" style="width: 100%">
            <el-option label="工作" value="工作" />
            <el-option label="学习" value="学习" />
            <el-option label="生活" value="生活" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" v-if="currentTask.id">
          <el-select v-model="currentTask.status" style="width: 100%">
            <el-option label="待办" :value="0" />
            <el-option label="未完成" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="currentTask.priority" style="width: 100%">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="currentTask.dueDate" type="datetime" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Delete, User, Search, Clock, Edit, Select, Close } from '@element-plus/icons-vue'
import { getTasks, createTask, updateTask, deleteTask, searchTasks, getOverdueTasks } from '../api/task'
import { useUserStore } from '../stores/user'
import dayjs from 'dayjs'
import confetti from 'canvas-confetti'

const router = useRouter()
const userStore = useUserStore()
const tasks = ref([])
const overdueTasks = ref([])
const showDialog = ref(false)
const searchKeyword = ref('')
const filter = reactive({ category: null, status: null, priority: null, sortBy: 'createTime' })
const currentTask = reactive({ id: null, title: '', description: '', category: '工作', status: 0, priority: 1, dueDate: null, userId: 1 })

const soonDueTasks = computed(() => {
  const now = new Date().getTime()
  const oneDayMs = 86400000
  return tasks.value.filter((task: any) => {
    if (task.status === 2 || !task.dueDate) return false
    const dueTime = new Date(task.dueDate).getTime()
    return dueTime > now && dueTime - now <= oneDayMs
  })
})

const isOverdue = (task: any) => {
  if (task.status === 2 || !task.dueDate) return false
  return new Date(task.dueDate).getTime() < new Date().getTime()
}

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

const editTask = (task: any) => {
  Object.assign(currentTask, task)
  showDialog.value = true
}

const handleSave = async () => {
  if (currentTask.id) {
    await updateTask(currentTask.id, currentTask)
    ElMessage.success('更新成功')
  } else {
    await createTask(currentTask)
    ElMessage.success('创建成功')
  }
  showDialog.value = false
  Object.assign(currentTask, { id: null, title: '', description: '', category: '工作', status: 0, priority: 1, dueDate: null, userId: 1 })
  loadTasks()
}

const handleDelete = async (id: string) => {
  await ElMessageBox.confirm('确认删除？')
  await deleteTask(id)
  ElMessage.success('已移至回收站')
  loadTasks()
}

const toggleStatus = async (task: any, status: number) => {
  await updateTask(task.id, { ...task, status })
  if (status === 2) {
    confetti({
      particleCount: 100,
      spread: 70,
      origin: { y: 0.6 }
    })
  }
  ElMessage.success(status === 2 ? '完成！！！' : '未完成！！！')
  loadTasks()
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const logout = () => {
  userStore.clearToken()
  router.push('/login')
}

onMounted(() => {
  loadTasks()
  loadOverdueTasks()
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

.message-content {
  flex: 1;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #d1d1d1;
  padding: 16px 20px 16px 28px;
  position: relative;
  overflow: hidden;
}

.ribbon {
  position: absolute;
  top: 12px;
  right: -35px;
  width: 110px;
  padding: 6px 0;
  text-align: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  transform: rotate(45deg);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
  letter-spacing: 1px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.ribbon-incomplete {
  background: linear-gradient(135deg, #ffa726 0%, #fb8c00 100%);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.ribbon-complete {
  background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.ribbon-overdue {
  background: linear-gradient(135deg, #ef5350 0%, #e53935 100%);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.breathing-light {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  animation: breathe 2s ease-in-out infinite;
}

.breathing-light.priority-1 {
  background: #67c23a;
  box-shadow: 0 0 8px #67c23a;
}

.breathing-light.priority-2 {
  background: #e6a23c;
  box-shadow: 0 0 8px #e6a23c;
}

.breathing-light.priority-3 {
  background: #f56c6c;
  box-shadow: 0 0 8px #f56c6c;
}

@keyframes breathe {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.4;
    transform: scale(0.8);
  }
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #202123;
}

.task-status-actions {
  display: flex;
  gap: 8px;
}

.task-meta-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.status-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  border-radius: 6px;
  border: none;
  font-size: 13px;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
}

.status-btn.complete {
  background: #67c23a;
}

.status-btn.complete:hover {
  background: #85ce61;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.status-btn.incomplete {
  background: #e6a23c;
}

.status-btn.incomplete:hover {
  background: #ebb563;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
}

.task-desc {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #40414f;
  line-height: 1.6;
}

.task-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.task-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #8e8ea0;
}

.task-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  padding-bottom: 12px;
  border-top: 1px solid #e5e5e5;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #d1d1d1;
  background: transparent;
  font-size: 13px;
  color: #202123;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #ececec;
}

.action-btn.delete {
  color: #ef4444;
  border-color: #ef4444;
}

.action-btn.delete:hover {
  background: #fef2f2;
}

.input-area {
  padding: 16px 24px 24px;
  border-top: 1px solid #ececec;
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
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
