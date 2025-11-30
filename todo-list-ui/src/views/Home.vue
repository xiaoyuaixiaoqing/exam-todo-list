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
          <el-option label="进行中" :value="1" />
          <el-option label="已完成" :value="2" />
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
        </el-alert>

        <div class="message-list">
          <div v-for="task in tasks" :key="task.id" class="message-item">
            <div class="message-content">
              <div class="task-header">
                <h3>{{ task.title }}</h3>
                <el-tag :type="['info', '', 'success'][task.status]" size="small" effect="plain">
                  {{ ['待办', '进行中', '已完成'][task.status] }}
                </el-tag>
              </div>
              <p class="task-desc">{{ task.description || '暂无描述' }}</p>
              <div class="task-meta">
                <el-tag size="small" effect="plain">{{ task.category }}</el-tag>
                <el-tag :type="['info', 'warning', 'danger'][task.priority - 1]" size="small" effect="plain">
                  {{ ['低', '中', '高'][task.priority - 1] }}
                </el-tag>
                <span class="task-date" v-if="task.dueDate">
                  <el-icon><Clock /></el-icon>
                  {{ task.dueDate }}
                </span>
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
        <el-form-item label="状态">
          <el-select v-model="currentTask.status" style="width: 100%">
            <el-option label="待办" :value="0" />
            <el-option label="进行中" :value="1" />
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Delete, User, Search, Clock, Edit } from '@element-plus/icons-vue'
import { getTasks, createTask, updateTask, deleteTask, searchTasks, getOverdueTasks } from '../api/task'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const tasks = ref([])
const overdueTasks = ref([])
const showDialog = ref(false)
const searchKeyword = ref('')
const filter = reactive({ category: null, status: null, priority: null, sortBy: 'createTime' })
const currentTask = reactive({ id: null, title: '', description: '', category: '工作', status: 0, priority: 1, dueDate: null, userId: 1 })

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
  padding: 16px 20px;
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
  margin-bottom: 12px;
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
