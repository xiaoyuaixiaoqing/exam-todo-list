<template>
  <el-dialog :model-value="visible" title="版本冲突" width="700px" @close="$emit('close')">
    <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
      该任务已被其他用户修改，请选择要保留的版本
    </el-alert>

    <div class="conflict-compare">
      <div class="version-column">
        <h3>我的修改</h3>
        <div class="version-content">
          <div v-for="field in fields" :key="field.key" class="field-item">
            <div class="field-label">{{ field.label }}:</div>
            <div class="field-value" :class="{ changed: isChanged(field.key) }">
              {{ formatValue(field.key, myVersion[field.key]) }}
            </div>
          </div>
        </div>
      </div>

      <div class="version-column">
        <h3>服务器版本</h3>
        <div class="version-content">
          <div v-for="field in fields" :key="field.key" class="field-item">
            <div class="field-label">{{ field.label }}:</div>
            <div class="field-value" :class="{ changed: isChanged(field.key) }">
              {{ formatValue(field.key, serverVersion[field.key]) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button @click="$emit('resolve', 'server')">保留服务器版本</el-button>
      <el-button type="primary" @click="$emit('resolve', 'mine')">保留我的修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'

const props = defineProps<{
  visible: boolean
  myVersion: any
  serverVersion: any
}>()

defineEmits<{
  close: []
  resolve: [choice: string]
}>()

const fields = [
  { key: 'title', label: '标题' },
  { key: 'description', label: '描述' },
  { key: 'category', label: '分类' },
  { key: 'status', label: '状态' },
  { key: 'priority', label: '优先级' },
  { key: 'dueDate', label: '截止日期' }
]

const statusNames: any = { 0: '待办', 1: '未完成', 2: '已完成', 4: '已超期' }
const priorityNames: any = { 1: '低', 2: '中', 3: '高' }

const isChanged = (key: string) => {
  return props.myVersion[key] !== props.serverVersion[key]
}

const formatValue = (key: string, value: any) => {
  if (!value) return '无'
  if (key === 'status') return statusNames[value] || value
  if (key === 'priority') return priorityNames[value] || value
  if (key === 'dueDate') return dayjs(value).format('YYYY-MM-DD HH:mm')
  return value
}
</script>

<style scoped>
.conflict-compare {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.version-column h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.version-content {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  background: #f5f7fa;
}

.field-item {
  margin-bottom: 12px;
}

.field-item:last-child {
  margin-bottom: 0;
}

.field-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.field-value {
  font-size: 14px;
  color: #606266;
  padding: 4px 8px;
  border-radius: 4px;
  background: #fff;
}

.field-value.changed {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  color: #f56c6c;
  font-weight: 500;
}
</style>
