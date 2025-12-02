<template>
  <div class="recycle-container">
    <div class="header">
      <div class="header-left">
        <el-button text @click="router.push('/home')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>回收站</h2>
      </div>
    </div>

    <div class="main-content">
      <el-alert type="info" :closable="false" class="info-alert" show-icon>
        回收站中的任务将在15天后自动永久删除
      </el-alert>

      <div class="task-list">
        <div v-for="task in tasks" :key="task.id" class="task-item">
          <div class="task-content">
            <div class="task-title-row">
              <h4>{{ task.title }}</h4>
              <el-tag :type="['info', '', 'success'][task.status]" size="small" effect="plain">
                {{ ['待办', '进行中', '已完成'][task.status] }}
              </el-tag>
            </div>
            <p class="task-description">{{ task.description || '暂无描述' }}</p>
            <div class="task-footer">
              <div class="task-tags">
                <el-tag size="small" effect="plain">{{ task.category }}</el-tag>
                <el-tag :type="['info', 'warning', 'danger'][task.priority - 1]" size="small" effect="plain">
                  {{ ['低', '中', '高'][task.priority - 1] }}
                </el-tag>
              </div>
              <div class="task-actions">
                <el-button size="small" @click="handleRestore(task.id)">
                  <el-icon><RefreshRight /></el-icon>
                  恢复
                </el-button>
                <el-button size="small" type="danger" @click="handlePermanentDelete(task.id)">
                  <el-icon><Delete /></el-icon>
                  永久删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="tasks.length === 0" description="回收站为空" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Delete, RefreshRight } from '@element-plus/icons-vue'
import { getRecycleBin, restoreTask, permanentDeleteTask } from '../api/task'
import type { Task } from '../types/task'

const router = useRouter()
const tasks = ref<Task[]>([])

const loadTasks = async () => {
  const res: any = await getRecycleBin(1)
  tasks.value = res.data
}

const handleRestore = async (id: string) => {
  await restoreTask(id)
  ElMessage.success('恢复成功')
  loadTasks()
}

const handlePermanentDelete = async (id: string) => {
  await ElMessageBox.confirm('确认永久删除？此操作不可恢复！', '警告', { type: 'warning' })
  await permanentDeleteTask(id)
  ElMessage.success('永久删除成功')
  loadTasks()
}

onMounted(loadTasks)
</script>

<style scoped>
.recycle-container {
  min-height: 100vh;
  background: #f7f7f8;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 20px 40px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #6e6e80;
  padding: 8px 12px;
}

.back-btn:hover {
  background: #f7f7f8;
}

.header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #202123;
}

.main-content {
  padding: 20px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.info-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 16px 20px;
  transition: all 0.2s;
}

.task-item:hover {
  border-color: #d0d0d0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.task-content {
  width: 100%;
}

.task-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-title-row h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #202123;
}

.task-description {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #6e6e80;
  line-height: 1.5;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-tags {
  display: flex;
  gap: 8px;
  align-items: center;
}

.task-actions {
  display: flex;
  gap: 8px;
}
</style>
