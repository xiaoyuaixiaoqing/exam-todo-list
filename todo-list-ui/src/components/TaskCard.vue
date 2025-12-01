<template>
  <div class="message-content" :class="'status-' + task.status">
    <div class="breathing-light" :class="'priority-' + task.priority"></div>
    <div v-if="task.status === 1" class="ribbon ribbon-incomplete">未完成</div>
    <div v-if="task.status === 2" class="ribbon ribbon-complete">已完成</div>
    <div v-if="isOverdue" class="ribbon ribbon-overdue">已超期</div>
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
        <button class="status-btn complete" @click="$emit('toggle-status', task, 2)">
          <el-icon><Select /></el-icon>
          完成
        </button>
        <button class="status-btn incomplete" @click="$emit('toggle-status', task, 1)">
          <el-icon><Close /></el-icon>
          未完成
        </button>
      </div>
    </div>
    <div class="task-actions">
      <button class="action-btn" @click="$emit('edit', task)">
        <el-icon><Edit /></el-icon>
        编辑
      </button>
      <button class="action-btn delete" @click="$emit('delete', task.id)">
        <el-icon><Delete /></el-icon>
        删除
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Clock, Edit, Delete, Select, Close } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const props = defineProps<{
  task: any
}>()

defineEmits<{
  edit: [task: any]
  delete: [id: string]
  'toggle-status': [task: any, status: number]
}>()

const isOverdue = computed(() => {
  if (props.task.status === 2 || !props.task.dueDate) return false
  return new Date(props.task.dueDate).getTime() < new Date().getTime()
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
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

.task-desc {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #40414f;
  line-height: 1.6;
}

.task-meta-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
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

.task-status-actions {
  display: flex;
  gap: 8px;
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
</style>
